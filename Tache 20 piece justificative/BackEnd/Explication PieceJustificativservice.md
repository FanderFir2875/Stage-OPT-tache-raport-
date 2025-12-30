# Service métier – `PieceJustificativeService`

## Fichier concerné

```
src/main/java/nc/opt/sior/service/PieceJustificativeService.java
```

---

## Rôle du service

Le service `PieceJustificativeService` porte la **logique métier complète** liée aux pièces justificatives.

Il est responsable de :

* la **validation métier** (existence de la demande OR)
* la **coordination entre la base de données et MinIO**
* la **gestion transactionnelle** pour garantir la cohérence des données

C’est le point central entre :

* le stockage objet (MinIO),
* la persistance JPA,
* le domaine métier OR.

---

## Déclaration du service

```java
@Service
public class PieceJustificativeService {
```

* Annoté avec `@Service` (composant Spring)
* Injecté dans le controller REST
* Annoté `@Transactional` sur les méthodes critiques

---

## Dépendances injectées

```java
private final DemandeOrRepository demandeOrRepository;
private final PieceJustificativeRepository pieceJustificativeRepository;
private final PieceJustificativeStorageService storageService;
```

| Dépendance                         | Rôle                                  |
| ---------------------------------- | ------------------------------------- |
| `DemandeOrRepository`              | Vérifier l’existence de la demande OR |
| `PieceJustificativeRepository`     | Persister / supprimer les métadonnées |
| `PieceJustificativeStorageService` | Gérer le stockage MinIO               |

---

## Ajout d’une pièce justificative

## Signature

```java
@Transactional
public PieceJustificative ajouterPiece(
    Long demandeOrId,
    MultipartFile file,
    TypePieceJustificative type
)
```

---

## Déroulement détaillé

###  Vérification de la demande OR

```java
DemandeOr demande = demandeOrRepository.findById(demandeOrId)
    .orElseThrow(() -> new IllegalArgumentException(
        "Demande OR introuvable : " + demandeOrId
    ));
```

* Empêche l’ajout d’une pièce sur une demande inexistante
* Sécurise la cohérence métier dès l’entrée du service

---

### Upload du fichier dans MinIO

```java
String objectName = storageService.upload(file, demandeOrId);
```

* Le fichier est stocké physiquement dans MinIO
* Le `objectName` généré est conservé pour la base de données

---

### Construction de l’entity `PieceJustificative`

```java
PieceJustificative piece = new PieceJustificative();
piece.setDemandeOr(demande);
piece.setType(type);
piece.setNomFichier(file.getOriginalFilename());
piece.setContentType(file.getContentType());
piece.setTaille(file.getSize());
piece.setObjectName(objectName);
```

* Association à la demande OR
* Enregistrement des métadonnées du fichier
* Lien explicite avec l’objet MinIO

---

### Persistance en base avec gestion des erreurs

```java
try {
    return pieceJustificativeRepository.save(piece);
} catch (RuntimeException e) {

    storageService.delete(objectName);
    throw e;
}
```

* Si la persistance échoue :

  * le fichier est **supprimé de MinIO**
  * l’exception est relancée
* Garantit une **cohérence totale** entre la base et MinIO

---

## Suppression d’une pièce justificative

### Signature

```java
@Transactional
public void supprimerPiece(Long pieceId)
```

---

### Déroulement

####  Récupération de la pièce

```java
PieceJustificative piece = pieceJustificativeRepository.findById(pieceId)
    .orElseThrow(() -> new IllegalArgumentException(
        "Pièce introuvable : " + pieceId
    ));
```

---

####  Suppression du fichier MinIO

```java
storageService.delete(piece.getObjectName());
```

---

####  Suppression en base

```java
pieceJustificativeRepository.delete(piece);
```

---

### Gestion transactionnelle

* Les méthodes sont annotées `@Transactional`
* Garantit :

  * atomicité des opérations
  * cohérence base ↔ stockage
  * rollback en cas d’erreur

---

### Résumé

* Service métier central et robuste
* Coordination propre entre :

  * base de données
  * stockage MinIO
* Gestion des erreurs et rollback maîtrisée
* Séparation claire des responsabilités

---

**PieceJustificativeService :**

```java
package nc.opt.sior.service;

import nc.opt.sior.domain.DemandeOr;
import nc.opt.sior.domain.PieceJustificative;
import nc.opt.sior.domain.enumeration.TypePieceJustificative;
import nc.opt.sior.repository.DemandeOrRepository;
import nc.opt.sior.repository.PieceJustificativeRepository;
import nc.opt.sior.service.storage.PieceJustificativeStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PieceJustificativeService {

    private final DemandeOrRepository demandeOrRepository;
    private final PieceJustificativeRepository pieceJustificativeRepository;
    private final PieceJustificativeStorageService storageService;

    public PieceJustificativeService(
        DemandeOrRepository demandeOrRepository,
        PieceJustificativeRepository pieceJustificativeRepository,
        PieceJustificativeStorageService storageService
    ) {
        this.demandeOrRepository = demandeOrRepository;
        this.pieceJustificativeRepository = pieceJustificativeRepository;
        this.storageService = storageService;
    }

    @Transactional
    public PieceJustificative ajouterPiece(Long demandeOrId, MultipartFile file, TypePieceJustificative type) {

        DemandeOr demande = demandeOrRepository.findById(demandeOrId)
            .orElseThrow(() -> new IllegalArgumentException("Demande OR introuvable : " + demandeOrId));


        String objectName = storageService.upload(file, demandeOrId);


        PieceJustificative piece = new PieceJustificative();
        piece.setDemandeOr(demande);
        piece.setType(type);
        piece.setNomFichier(file.getOriginalFilename());
        piece.setContentType(file.getContentType());
        piece.setTaille(file.getSize());
        piece.setObjectName(objectName);

        try {
            return pieceJustificativeRepository.save(piece);
        } catch (RuntimeException e) {

            storageService.delete(objectName);
            throw e;
        }
    }

    @Transactional
    public void supprimerPiece(Long pieceId) {
        PieceJustificative piece = pieceJustificativeRepository.findById(pieceId)
            .orElseThrow(() -> new IllegalArgumentException("Pièce introuvable : " + pieceId));


        storageService.delete(piece.getObjectName());


        pieceJustificativeRepository.delete(piece);
    }
}
```