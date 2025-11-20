#  **Récapitulatif Technique – Fonctionnalité : Consultation complète d’une demande OR (Admin)**

## **Objectif**

Permettre à un administrateur de consulter **toutes les informations complètes** d’une demande d’Ordre de Réexpédition (OR), telles que saisies dans le formulaire utilisateur.

Cela inclut :

* Informations du demandeur
* Ancienne / nouvelle adresse
* Paramètres de réexpédition
* Options (BP / CEDEX / commentaires…)
* Dates, statut, Audit JPA ,  etc.

---

# **Création du DTO de détail : `DemandeOrDetailDTO`**

on crée un DTO dédié pour la vue admin afin d’exposer **toutes** les informations d’une demande OR dans un format lisible.

```java
@Data
public class DemandeOrDetailDTO {
    private long id;
    private String statut;

    // Demandeur
    private String demandeurNom;
    private String demandeurPrenom;
    private String demandeurEmail;
    private String demandeurTelephone;
 
    // Adresses
    private AdresseDTO ancienneAdresse;
    private AdresseDTO nouvelleAdresse;


    // Réexpédition
    private String typeReexpedition;
    private Instant dateDebut;
    private Instant dateFin;


    // Audit JPA
    private Instant createdDate;
    private Instant lastModifiedDate;


    // Options
    private OptionsDTO options;
}
```

---

# 2️ **Création du DTO des options : `OptionsDTO`**

Simplifié pour représenter l’ensemble des choix optionnels utilisateur :

```java
@Data
public class OptionsDTO {
    private String bp;
    private String cedex;
    private String commentaire;
}
```

---

# 3️ **Réutilisation du DTO existant : `AdresseDTO`**

`AdresseDTO` (Présent dans le projet de base est suffisant) est suffisamment complet pour afficher les informations d’adresse utilisateur.

---

# 4️ **Ajout du service métier : `getDetailForAdmin(id)` dans `OrDematService`**

On ajoute une méthode dans le service métier (orchestration) pour :

- Récupérer l'entité complète via `demandeOrService.findById()`
- Lire les valeurs JSON stockées dans `demande_or.data`
- Remplir le DTO détaillé avec les données récupérées
- Retourner un objet nettoyé et lisible pour le front-end


### Exemple simplifié :

```java
public DemandeOrDetailDTO getDetailForAdmin(Long id) {
    DemandeOr demande = demandeOrService.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Demande OR non trouvée : " + id));

    JsonNode json = demande.getData();

    DemandeOrDetailDTO dto = new DemandeOrDetailDTO();
    dto.setId(demande.getId());
    dto.setStatut(JsonUtils.getStringOrDefault(json, "statut", "INCONNNU"));

    // Demandeur
    dto.setDemandeurNom(JsonUtils.getStringOrDefault(json, "demandeurNom", null));
    dto.setDemandeurPrenom(JsonUtils.getStringOrDefault(json, "demandeurPrenom", null));
    dto.setDemandeurEmail(JsonUtils.getStringOrDefault(json, "demandeurEmail", null));
    dto.setDemandeurTelephone(JsonUtils.getStringOrDefault(json, "demandeurTelephone", null));

    // Adresses
    dto.setAncienneAdresse(mapperAdresse(json.get("ancienneAdresse")));
    dto.setNouvelleAdresse(mapperAdresse(json.get("nouvelleAdresse")));

    // Réexpédition
    dto.setTypeReexpedition(JsonUtils.getStringOrDefault(json, "typeReexpedition", null));
    dto.setDateDebut(demande.getDateDebut());
    dto.setDateFin(demande.getDateFin());

    // Audit JPA
    dto.setCreatedDate(demande.getCreatedDate());
    dto.setLastModifiedDate(demande.getLastModifiedDate());

    // Options
    dto.setOptions(mapperOptions(json.get("options")));

    return dto;
}
```

---

# 5️ **Création des méthodes utilitaires (mapperAdresse / mapperOptions)**

On isole la logique de reconstruction des sous-objets JSON → DTO
Pour éviter des classes trop lourdes, ces méthodes doivent être placées dans un **service dédié** (bonne pratique) :

`service/mapper`

* `DemandeOrAdresseMapper`
* `DemandeOrOptionsMapper`

Cela rend le code plus propre, plus testable et plus clair.

---

# 6️⃣ **Modification du repository / service persistence**

### Ajout dans le service

```java
public Optional<DemandeOr> findById(Long id) {
    return demandeOrRepository.findById(id);
}
```

### Pas besoin de modifier le repository (hérite déjà de `findById`).

---

# 7️⃣ **Ajout du nouveau endpoint dans `OrDematResource`**

L’objectif : exposer le détail complet d’une demande pour l’admin.

```java
@GetMapping("/or-demandes/{id}")
public ResponseEntity<DemandeOrDetailDTO> getDetail(@PathVariable Long id) {
    return ResponseEntity.ok(orDematService.getDetailForAdmin(id));
}
```

✔ URL REST :

```
GET /api/demat/or-demandes/{id}
```

---

# 8️⃣ **Tests Postman**

Nous avons commencé à tester :

### ✔ `POST /api/demat/or-definitif`

→ insertion OK mais erreurs 500 si format JSON incorrect

### ✔ `GET /api/demat/or-demande/{id}`

→ en cours de test maintenant que la structure DTO est en place

---

# 9️⃣ **Préparation Front (à venir)**

Dans une 2ème partie, nous devrons :

1. Ajouter un bouton “Voir détail” dans la liste admin
2. Créer un composant Angular `DemandeOrDetailComponent`
3. Appeler le nouveau endpoint
4. Afficher les données sous forme de blocs structurés

   * Bloc Demandeur
   * Bloc Adresses
   * Bloc Paramètres
   * Bloc Options
   * Bloc Audit


---