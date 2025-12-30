# Entity JPA – `PieceJustificative`

## Fichier concerné

```
src/main/java/nc/opt/sior/domain/PieceJustificative.java
```

---

### Rôle de l’entity

L’entity `PieceJustificative` représente **une pièce justificative associée à une demande d’Ordre de Réexpédition**.

Elle ne contient **pas le fichier lui-même**, mais uniquement :

* ses **métadonnées**
* son **lien avec la demande OR**
* la **référence vers l’objet stocké dans MinIO**

Cette entity permet de faire le lien entre :

* la **base de données relationnelle**
* le **stockage objet MinIO**
* la **logique métier OR**


---

### Déclaration de l’entity

```java
@Entity
@Table(name = "piece_justificative")
public class PieceJustificative {
```

* L’entity est mappée sur la table `piece_justificative`
* Chaque instance correspond à **une ligne de cette table**

---

### Identifiant

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

* Identifiant technique unique
* Généré automatiquement par la base de données

---

### Type de pièce justificative

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private TypePieceJustificative type;
```

* Type fonctionnel de la pièce justificative
* Basé sur l’enum `TypePieceJustificative`
* Stocké en base sous forme **textuelle** (`STRING`)
* Garantit la cohérence des types entre le frontend et le backend

---

### Nom du fichier

```java
@Column(name = "nom_fichier", nullable = false)
private String nomFichier;
```

* Nom original du fichier transmis par l’utilisateur
* Utilisé pour l’affichage ou la traçabilité

---

### Type MIME

```java
@Column(name = "content_type", nullable = false)
private String contentType;
```

* Type MIME du fichier (ex : `application/pdf`, `image/png`)
* Permet d’identifier la nature du fichier stocké

---

### Taille du fichier

```java
@Column(name = "taille", nullable = false)
private Long taille;
```

* Taille du fichier en octets
* Utile pour les contrôles, quotas ou audits

---

### Référence MinIO

```java
@Column(name = "object_name", nullable = false)
private String objectName;
```

* Identifiant technique du fichier dans MinIO
* Correspond au nom de l’objet stocké dans le bucket
* Généré côté backend (ex : UUID + chemin logique)

---

### Relation avec la demande OR

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "demande_or_id")
private DemandeOr demandeOr;
```

* Relation **Many-to-One** avec l’entity `DemandeOr`
* Chaque pièce justificative est rattachée à **une seule demande OR**
* Chargement en `LAZY` pour éviter les surcharges inutiles
* La clé étrangère est stockée dans la colonne `demande_or_id`

---

### Lombok

```java
@Data
```

* Génère automatiquement :

  * getters / setters
  * `toString()`
  * `equals()` / `hashCode()`
* Réduit le code boilerplate

---

### Résumé

* `PieceJustificative` représente **les métadonnées d’un fichier**
* Le fichier réel est stocké dans **MinIO**
* La relation avec `DemandeOr` est obligatoire
* L’entity est conçue pour être :

  * claire
  * extensible
  * cohérente avec le modèle métier

---

**PieceJustificative :** 
```java
package nc.opt.sior.domain;

import lombok.Data;
import nc.opt.sior.domain.enumeration.TypePieceJustificative;

import javax.persistence.*;

@Data
@Entity
@Table(name = "piece_justificative")
public class PieceJustificative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePieceJustificative type;

    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "taille", nullable = false)
    private Long taille;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_or_id")
    private DemandeOr demandeOr;


}

```