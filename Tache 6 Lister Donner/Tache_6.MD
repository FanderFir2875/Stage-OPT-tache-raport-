# **Documentation — Données nécessaires à la création d’un OR définitif**

---

## **Objectif**

> Identifier, documenter et tracer **toutes les données nécessaires** à la création d’un **Ordre de Réexpédition (OR) définitif** dans le système **SIOR**, en précisant :
>
> * Les champs requis et leurs types
> * Les sources de données (entité, table, DTO, endpoint)
> * Les contraintes fonctionnelles et techniques
> * Les dépendances éventuelles entre champs

---

## **1. Contexte**

Un **Ordre de Réexpédition (OR)** est un contrat postal permettant de rediriger le courrier d’un client vers une nouvelle adresse.
L’**OR définitif** correspond à la version validée et enregistrée du contrat après saisie complète et vérification.

L’objectif est de garantir :

* la **cohérence des données saisies** (dates, adresses, statut)
* la **traçabilité complète** du dossier (titulaire, agence, audit)
* la **sécurisation du stockage** via JPA et Liquibase

---

## **2. Structure des données – Vue d’ensemble**

| Source               | Description                                                    |
| -------------------- | -------------------------------------------------------------- |
| **`demande_or`**     | Contient les dates et informations de base liées à la demande  |
| **`contrat`**        | Définit le type, le numéro et le statut de l’OR                |
| **`personne`**       | Détient les informations du titulaire et des bénéficiaires     |
| **`adresse`**        | Stocke les adresses d’origine et de destination                |
| **`agence_postale`** | Identifie l’agence gérant la réexpédition                      |
| **`auditing`**       | Gère les métadonnées de création/modification                  |
| **`OrDematDTO`**     | Structure JSON utilisée par le front pour la création initiale |

---

## **3. Tableau détaillé des champs nécessaires à un OR définitif**

| **Nom du champ**        | **Type Java** | **Type SQL**   | **Source**       | **Description fonctionnelle**                       | **Contraintes**          | **Obligatoire** |
| ----------------------- | ------------- | -------------- | ---------------- | --------------------------------------------------- | ------------------------ | --------------- |
| `id`                    | `Long`        | `BIGSERIAL`    | `demande_or`     | Identifiant unique de la demande d’OR               | PK                       | ✅               |
| `dateDebut`             | `Instant`     | `TIMESTAMP`    | `demande_or`     | Début de validité de l’ordre                        | NOT NULL                 | ✅               |
| `dateFin`               | `Instant`     | `TIMESTAMP`    | `demande_or`     | Fin de validité de l’ordre                          | `> dateDebut`            | ✅               |
| `typeOr`                | `String`      | `VARCHAR(20)`  | `contrat`        | Type d’ordre : `TEMPORAIRE` / `DEFINITIF`           | Enum                     | ✅               |
| `nomTitulaire`          | `String`      | `VARCHAR(255)` | `personne`       | Nom du titulaire                                    | NOT NULL                 | ✅               |
| `prenomTitulaire`       | `String`      | `VARCHAR(255)` | `personne`       | Prénom du titulaire                                 | NOT NULL                 | ✅               |
| `civilite`              | `String`      | `VARCHAR(10)`  | `personne`       | Civilité (`M`, `Mme`, `Mlle`)                       | Valeur contrôlée         | ❌               |
| `numContrat`            | `String`      | `VARCHAR(50)`  | `contrat`        | Numéro de contrat OR                                | UNIQUE                   | ✅               |
| `adresseDepart`         | `String`      | `VARCHAR(255)` | `adresse`        | Adresse actuelle du titulaire                       | NOT NULL                 | ✅               |
| `adresseDestination`    | `String`      | `VARCHAR(255)` | `adresse`        | Nouvelle adresse de réexpédition                    | NOT NULL                 | ✅               |
| `codePostalDestination` | `String`      | `VARCHAR(10)`  | `adresse`        | Code postal du lieu de destination                  | Vérifié SIG Postal       | ✅               |
| `villeDestination`      | `String`      | `VARCHAR(255)` | `adresse`        | Commune de destination                              | —                        | ✅               |
| `agenceGestionnaire`    | `String`      | `VARCHAR(255)` | `agence_postale` | Agence postale gérant l’ordre                       | FK → `agence_postale.id` | ✅               |
| `motif`                 | `String`      | `VARCHAR(255)` | `contrat`        | Motif du transfert (facultatif)                     | —                        | ❌               |
| `resiliationDate`       | `Instant`     | `TIMESTAMP`    | `contrat`        | Date de résiliation si applicable                   | NULLABLE                 | ❌               |
| `isResilie`             | `Boolean`     | `BOOLEAN`      | `contrat`        | Indicateur de résiliation                           | Default `false`          | ❌               |
| `statutOr`              | `String`      | `VARCHAR(50)`  | `contrat`        | Statut de l’ordre (`EN_COURS`, `VALIDE`, `RESILIE`) | Enum                     | ✅               |
| `createdBy`             | `String`      | `VARCHAR(50)`  | `auditing`       | Créateur du dossier                                 | Auto                     | ✅               |
| `createdDate`           | `Instant`     | `TIMESTAMP`    | `auditing`       | Date de création                                    | Auto                     | ✅               |
| `lastModifiedBy`        | `String`      | `VARCHAR(50)`  | `auditing`       | Dernier modificateur                                | Auto                     | ❌               |
| `lastModifiedDate`      | `Instant`     | `TIMESTAMP`    | `auditing`       | Date de modification                                | Auto                     | ❌               |

---

## **4. Champs dépendants ou conditionnels**

| **Champ**            | **Condition / Dépendance**                       | **Règle**                      |
| -------------------- | ------------------------------------------------ | ------------------------------ |
| `resiliationDate`    | Renseigné uniquement si `statutOr = 'RESILIE'`   | Contrôle logique en service    |
| `motif`              | Facultatif sauf pour certains types de transfert | Déterminé par le front         |
| `civilite`           | Non bloquant, mais présent dans les courriers    | Normalisé (`M`, `Mme`, `Mlle`) |
| `agenceGestionnaire` | Dérivée automatiquement via l’adresse postale    | Service SIG / Référentiel      |
| `isResilie`          | Calculé selon `resiliationDate`                  | Non modifiable côté front      |

---

## **5. Schéma logique des sources**

```text
              +----------------------+
              | PERSONNE (Titulaire) |
              +----------+-----------+
                         |
                         | nom, prénom, civilité
                         v
+-----------+    +-------+-------+     +----------------+
| DEMANDE_OR| -> | CONTRAT OR   | --> | ADRESSE        |
| (dates)   |    +-------+-------+     +----------------+
|           |            |
|           |            v
|           |     +----------------+
|           |     | AGENCE_POSTALE |
|           |     +----------------+
|           |
|           +--> AUDITING (createdBy, createdDate, etc.)
```

---

## **6. Contraintes et règles d’intégrité**

| Type                   | Détail                                                  |
| ---------------------- | ------------------------------------------------------- |
| **PK**                 | `id` auto-incrémentée                                   |
| **FK**                 | `agenceGestionnaire` → `agence_postale.id`              |
| **FK**                 | `adresseDestination` → `adresse.id`                     |
| **FK**                 | `titulaire` → `personne.id`                             |
| **Contraintes métier** | `dateFin > dateDebut`, `numContrat` unique              |
| **Statuts possibles**  | `EN_COURS`, `VALIDE`, `RESILIE`                         |
| **Audit**              | Champs `createdBy`, `createdDate` gérés automatiquement |

---

## **7. Exemple JSON (Frontend → Backend)**

```json
{
  "dateDebut": "2025-02-01",
  "dateFin": "2025-03-01",
  "nomTitulaire": "Dupont",
  "prenomTitulaire": "Marie",
  "adresseDepart": "12 Rue de la Poste",
  "adresseDestination": "18 Avenue du Parc",
  "codePostalDestination": "98800",
  "villeDestination": "Nouméa",
  "agenceGestionnaire": "Agence Principale Nouméa",
  "typeOr": "DEFINITIF"
}
```

---

## **8. Critères de validation / acceptation**

| Critère                                                     | Attendu | Vérification  |
| ----------------------------------------------------------- | ------- | ------------- |
| Tous les champs nécessaires à un OR définitif sont recensés | Oui     | Section 3     |
| Les sources de données sont identifiées                     | Oui     | Section 2 + 5 |
| Types et contraintes précisés                               | Oui     | Section 3     |
| Dépendances documentées                                     | Oui     | Section 4     |
| Schéma de flux complet                                      | Oui     | Section 5     |
| Format Confluence prêt à publication                        | Oui     | Ce document   |

---

## **9. Étapes suivantes**

| Étape | Action                                                                    |
| ----- | ------------------------------------------------------------------------- |
| 1️⃣   | Créer l’entité `OrdreReexpedition` complète avec toutes les relations JPA |
| 2️⃣   | Mettre à jour le `OrDefinitifDTO` et le `Mapper` associé                  |
| 3️⃣   | Documenter le nouvel endpoint `/api/or-definitif`                         |
| 4️⃣   | Préparer les tests d’intégration et de validation métier                  |
| 5️⃣   | Publier la documentation dans Confluence et notifier l’équipe métier      |

---

## **Références techniques**

* 🔗 [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
* 🔗 [Liquibase – Database Change Management](https://www.liquibase.org/)
* 🔗 [MapStruct Mapper Framework](https://mapstruct.org/)
* 🔗 [JHipster Architecture](https://www.jhipster.tech/architecture/)
* 🔗 [SIOR – Documentation interne](https://confluence.opt.nc/display/SIOR)

---

Souhaites-tu que je t’ajoute, juste après celle-ci, la **fiche complémentaire “Vue de flux des données OR (diagramme UML + mapping DTO ↔ Entité ↔ Table)”**, pour compléter la traçabilité visuelle sur Confluence ?
Ce serait la *phase 2* logique de cette nouvelle tâche.
