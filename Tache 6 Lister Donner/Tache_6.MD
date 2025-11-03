# Données nécessaires à la création d’un OR définitif



Documenter l’ensemble des **données nécessaires** à la création d’un **Ordre de Réexpédition (OR) définitif** dans le SIOR.  
Ce document permet d’assurer la traçabilité des informations entre les couches techniques (Entity, DTO, endpoint) et fonctionnelles (formulaire, validation, cycle de vie).

---

## Structure générale

L’OR définitif est représenté dans le système par l’entité `DemandeOr` (table `demande_or`).  
Cette entité est exposée via le DTO `OrDematDTO` et le service REST `/api/or-demat`.

---

## Tableau récapitulatif des données

| Champ | Description fonctionnelle | Type | Contraintes / Validation | Source technique | Commentaires |
|--------|----------------------------|-------|---------------------------|------------------|---------------|
| **id** | Identifiant unique de la demande | `Long` | Auto-généré (`@GeneratedValue`) | Table `demande_or` | Clé primaire |
| **numeroDemande** | Numéro interne de la demande d’OR | `String` | Unique, généré côté service | `OrDematService` | Sert de référence fonctionnelle |
| **typeOr** | Type d’OR demandé (TEMPORAIRE / DÉFINITIF) | `String` (Enum) | Non nul | `OrDematDTO` | Fixé à `DEFINITIF` pour ce module |
| **dateCreation** | Date de création de la demande | `LocalDateTime` | Généré automatiquement | `DemandeOrEntity` / BDD | Renseigné à la persistance |
| **dateActivation** | Date effective d’activation de la réexpédition | `LocalDate` | Peut être nulle tant que la demande n’est pas validée | `DemandeOrEntity` | Mise à jour à la validation |
| **dateCloture** | Date de fin de validité / résiliation | `LocalDate` | Optionnelle | `DemandeOrEntity` | Renseignée si clôture manuelle |
| **statut** | Statut fonctionnel de la demande (`EN_ATTENTE_VALIDATION`, `VALIDÉE`, `ACTIVE`, `CLOTURÉE`) | `String` (Enum) | Non nul, valeur par défaut `EN_ATTENTE_VALIDATION` | `DemandeOrEntity` | Géré par la logique métier |
| **titulaireNom** | Nom du titulaire de la réexpédition | `String` | Obligatoire | `OrDematDTO` / Front | Renseigné par le client |
| **titulairePrenom** | Prénom du titulaire | `String` | Obligatoire | `OrDematDTO` / Front | Renseigné par le client |
| **titulaireIdentifiant** | Identifiant du client dans le SIOR | `String` | Obligatoire, clé étrangère vers le référentiel client | `DemandeOrEntity` | Permet la liaison avec la base client |
| **adresseOrigine** | Adresse postale initiale du client | `String` | Obligatoire | `OrDematDTO` | Adresse source |
| **adresseDestination** | Nouvelle adresse de réexpédition (définitive) | `String` | Obligatoire | `OrDematDTO` | Adresse cible |
| **codeAgenceGestionnaire** | Code de l’agence postale ayant traité la demande | `String` | Obligatoire | `OrDematDTO` / API d’authentification | Déduit de l’utilisateur connecté |
| **justificatifDomicile** | Référence ou identifiant du document justificatif de domicile | `String` | Obligatoire pour l’activation | `OrDematDTO` | Validé par un agent avant activation |
| **dateValidation** | Date de validation par un agent | `LocalDateTime` | Nulle avant validation | `DemandeOrEntity` | Renseignée par le back-office |
| **agentValidation** | Identifiant de l’agent ayant validé la demande | `String` | Optionnelle | `DemandeOrEntity` | Déterminée lors de la validation |
| **motifCloture** | Raison de la clôture (si applicable) | `String` | Optionnelle | `DemandeOrEntity` | Renseignée lors de la clôture |
| **commentaire** | Commentaire libre de l’agent ou du client | `String` | Optionnelle | `OrDematDTO` | Utilisé pour suivi interne |

---

## Champs dérivés ou calculés  --- PAS SUR à vérifier.

| Champ | Source | Détail |
|--------|---------|--------|
| **numeroDemande** | Généré automatiquement dans `OrDematService` | Incrément logique ou UUID |
| **statut** | Défini automatiquement à `EN_ATTENTE_VALIDATION` | Changé via les transitions métier |
| **dateActivation** | Renseignée uniquement après validation de la demande | Calculée côté service |
| **dateCloture** | Renseignée à la clôture manuelle ou résiliation | Gérée par `cloturerDemandeOr()` |

---

## Correspondance technique (couches)

| Couche | Élément | Description |
|---------|----------|-------------|
| **Entity (JPA)** | `DemandeOr` | Représentation persistée dans PostgreSQL |
| **Repository** | `DemandeOrRepository` | Accès CRUD à la table `demande_or` |
| **Service** | `OrDematService` | Logique métier : création, validation, activation, clôture |
| **Mapper** | `OrDematMapper` | Conversion entre `OrDematDTO` ↔ `DemandeOrEntity` |
| **Controller** | `OrDematResource` | Endpoint REST `/api/or-demat` |
| **Liquibase** | `20251029-01_added_table_demande_or.xml` | Script de création de la table `demande_or` |
| **Tests** | `OrDematResourceIT` | Vérifie le flux complet de création et validation |

---





