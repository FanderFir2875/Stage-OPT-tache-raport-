# Documentation technique – Backend OR Dématérialisé → Création Contrat

### (Suite logique du document Frontend OR Physique → Physique)

---

## Objectif

Mettre en place la logique **backend** permettant :

* de recevoir une demande d’Ordre de Réexpédition (OR) réalisée en ligne
* de valider les données métier
* de persister une **Demande OR**
* de créer automatiquement un **Contrat OR**
* d’exposer ce contrat dans l’interface **back-office /contrat**

Cette documentation décrit la **conception**, le **workflow**, les **services**, les **mappings**, et les **choix techniques** réalisés.

---

## 1. Architecture backend – Vue d’ensemble

Le backend est développé en **Spring Boot / JHipster**, avec :

* Ressource REST : `OrDematResource`
* Services métier :

  * `OrDematService`
  * `OrDematContratService`
* Mappers :

  * `DemandeOrMapper`
* Entités :

  * `DemandeOr`
  * `Contrat`, `Titulaire`, `Adresse`
* Persistance JPA + JSONB

### Schéma logique simplifié

```
POST /api/demat/or-physique
        ↓
OrDematResource
        ↓
OrDematService
        ↓
DemandeOrMapper → DemandeOr (JSONB + Instant)
        ↓
DemandeOrService.save()
        ↓
OrDematContratService
        ↓
ContratService.saveContrat()
        ↓
Contrat visible dans /contrat (back-office)
```

---

## 2. Réception de la demande – OrDematRequestDTO

Le backend reçoit un **DTO issu du formulaire Angular**.

### Contenu principal :

* Dates : `dateDebut`, `dateFin` (format `yyyy-MM-dd`)
* Informations demandeur
* Ancienne et nouvelle adresse (JSON)

Ce DTO sert **exclusivement** à la validation et à la création de la Demande OR.

---

## 3. Validation métier – OrDematService

La méthode centrale est :

```java
createDemandeOr(OrDematRequestDTO dto)
```

### Règles métier appliquées

✔ dateDebut obligatoire
✔ dateFin obligatoire
✔ dateDebut ≥ date du jour
✔ dateFin > dateDebut
✔ adresses obligatoires
✔ ancienneAdresse ≠ nouvelleAdresse

### Gestion des dates

Les dates reçues étant **sans heure**, elles sont converties ainsi :

```java
LocalDate ld = LocalDate.parse(dto.getDateDebut());
Instant instant = ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
```

Cela évite les erreurs `Instant.parse()` et garantit une cohérence timezone.

---

## 4. Mapping vers DemandeOr – DemandeOrMapper

Le mapper transforme :

* DTO → Entity
* String → Instant
* Données formulaire → JSONB

### Conversion des dates

```java
public Instant toInstant(String date) {
    return LocalDate.parse(date)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant();
}
```

### Construction du champ `data` (JSONB)

```java
ObjectNode obj = objectMapper.createObjectNode();
obj.put("demandeurNom", dto.getDemandeurNom());
obj.set("ancienneAdresse", dto.getAncienneAdresse());
obj.set("nouvelleAdresse", dto.getNouvelleAdresse());
```

**Choix technique**
Le JSONB permet de conserver l’intégralité du formulaire sans multiplier les colonnes.

---

## 5. Entité DemandeOr

```java
@Entity
public class DemandeOr {

    private Instant dateDebut;
    private Instant dateFin;

    @Type(type = "jsonb")
    private JsonNode data;
}
```

- Une **Demande OR** représente une soumission en ligne
- Elle n’est **pas encore un contrat actif**

---

## 6. Création automatique du Contrat OR

Après la sauvegarde de `DemandeOr`, le service appelle :

```java
orDematContratService.creerContratDepuisDemande(saved);
```

---

## 7. OrDematContratService – Cœur de la fonctionnalité

Ce service :

* lit les données depuis `DemandeOr.data`
* crée toutes les entités nécessaires au Contrat OR

### Étapes détaillées

1. Création du `Contrat`
2. Initialisation du statut `DEMANDE_EN_LIGNE`
3. Création du `Titulaire`
4. Mapping des 2 `Adresse`
5. Assemblage via `ContratCompletDTO`
6. Persistance avec `ContratService.saveContrat()`

---

## 8. Contraintes JPA importantes (retours d’expérience)

### Champs obligatoires oubliés (Adresse)

Les entités `Adresse` imposent :

```java
@NotNull Boolean isTitulaire;
@NotNull Boolean isAuthorize;
```

**Obligation** lors du mapping :

```java
adresse.setIsTitulaire(false);
adresse.setIsAuthorize(false);
```

Sans cela → **RollbackException au commit**.

---

## 9. Statut du contrat OR

Le contrat est créé avec :

```java
contrat.setStatut(Statut.DEMANDE_EN_LIGNE);
```

📘 Ce statut permet :

* d’identifier une demande issue du web
* de la distinguer des contrats créés en agence
* de filtrer dans l’interface `/contrat`

À noter : `ContratService.saveContrat()` applique encore des règles legacy
à ajuster dans une évolution ultérieure.

---


## 10. Problèmes rencontrés & solutions

### Parsing de date (`Instant.parse()`)

✔ solution : `LocalDate → Instant`

---

### ZonedDateTime.from(Instant)

✔ solution :

```java
ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
```

---

### RollbackException JPA

✔ cause : champs `@NotNull` non initialisés
✔ correction : initialiser systématiquement les booléens

---
### Écrasement du statut `DEMANDE_EN_LIGNE`

✔ **Symptôme**

- La demande OR créée via le formulaire web :
  - crée bien une ligne en base dans la table `contrat`
  - **mais le statut final est `CREE` au lieu de `DEMANDE_EN_LIGNE`**
- Les logs montraient pourtant :
  > "Contrat OR DEMANDE_EN_LIGNE créé avec succès"

---

✔ **Cause**

Le statut était correctement positionné dans le service dématérialisé :

```java
contrat.setStatut(Statut.DEMANDE_EN_LIGNE);
````

Mais il était **écrasé plus tard** dans le service métier central :

```java
if (contrat.getId() == null) {
    addStatut(contrat);
}
```

Avec la méthode :

```java
private void addStatut(Contrat contrat) {
    contrat.setStatut(Statut.CREE);  // écrase le statut existant
}
```

➡️ Cette logique historique supposait que **toute création de contrat commençait en `CREE`**, ce qui n’est plus valable pour le canal web.

---

✔ **Solution**

Ne définir le statut par défaut **que s’il n’est pas déjà initialisé** :

```java
private void addStatut(Contrat contrat) {
    if (contrat.getStatut() == null) {
        contrat.setStatut(Statut.CREE);
    }
    contrat.setImprEtiquette(false);
    contrat.setImprFicheRecap(false);
}
```

---

✔ **Résultat**

* Création bureau → `CREE`
* Création dématérialisée → `DEMANDE_EN_LIGNE`
* Aucun impact sur le workflow existant
* Le statut final en base est désormais conforme à la logique métier

---
## Conclusion

Le backend OR dématérialisé :

* reçoit une demande en ligne
* valide les règles métier
* persiste une Demande OR
* crée automatiquement un Contrat OR
* rend ce contrat visible et consultable en back-office

Cette implémentation est **robuste**, **traçable**, et **alignée avec le workflow OR existant**.


