# Distinction entre OR Définitif et OR Temporaire

## Contexte général

Le SIOR (Système d’Information des Opérations de Réexpédition) gère plusieurs types d’Ordres de Réexpédition (OR) permettant aux clients de faire suivre leur courrier vers une nouvelle adresse.  
Ces ordres peuvent être **temporaires** (transfert pour une durée déterminée) ou **définitifs** (changement permanent d’adresse).


---

## Tableau comparatif

| Critère | OR Temporaire | OR Définitif |
|----------|----------------|---------------|
| **Objectif** | Permet de rediriger le courrier du client vers une autre adresse pour une période déterminée. | Permet de rediriger le courrier du client vers une nouvelle adresse de manière permanente (changement d’adresse officiel). |
| **Durée de validité** | Limitée dans le temps (date de début et date de fin définies dans le contrat). | Illimitée : l’adresse de destination devient l’adresse officielle du client. |
| **Nature du contrat** | Contrat temporaire, souvent associé à un retour automatique à l’adresse initiale à la fin de la période. | Contrat définitif : l’adresse source devient obsolète dans le système. |
| **Type de client concerné** | Clients absents temporairement (vacances, mutation, travaux…). | Clients ayant déménagé définitivement. |
| **Impact sur les données client** | Aucune modification permanente du dossier client (adresse d’origine conservée). | Mise à jour définitive de l’adresse principale du client dans les systèmes métiers. |
| **Cycle de vie du contrat** | Création → Activation → Expiration automatique → Clôture. | Création → Activation → Transformation en “nouvelle adresse de référence”. |
| **Annulation / Modification** | Possible avant ou pendant la période de validité. | Possible avant activation uniquement. |
| **Lien avec les autres modules SIOR** | Interaction limitée au module de réexpédition (aucun impact sur la base client). | Impact sur le module de gestion des contrats et la base client (adresse modifiée). |
| **Justificatifs requis** | Généralement aucun, sauf cas particuliers. | Justificatif de domicile obligatoire (preuve du déménagement). |
| **Exemples d’usage** | Déplacement temporaire pour une mission ou des vacances. | Déménagement définitif vers une nouvelle commune ou un autre territoire. |

---

## Implications techniques dans le SIOR

### Pour l’OR Temporaire
- Données gérées dans les modules existants (`contrat_or_temporaire`, `ordre_reexp_temp`).
- Logique métier centrée sur la **gestion des dates** (début/fin).
- Pas d’impact sur les référentiels clients ni sur les adresses permanentes.

### Pour l’OR Définitif
- Nouvelles données stockées dans la table `demande_or` (créée via Liquibase).
- Flux complet : `Entity → Repository → Service → Mapper → Controller`.
- Logique métier orientée **changement d’adresse permanent** :
  - Validation des pièces justificatives.
  - Création d’un OR définitif dans le système.
  - Mise à jour du référentiel client après activation.

---

## Synthèse visuelle (à venir)

Les **diagrammes de séquence** viendront compléter cette distinction :
- **Diagramme 1 : OR Temporaire** → Gestion du flux de création et expiration.  
- **Diagramme 2 : OR Définitif** → Gestion du flux de création, validation et mise à jour client.

---

## Conclusion

L’OR **temporaire** est un **service logistique à durée limitée**, tandis que l’OR **définitif** représente une **opération de changement d’adresse officielle**.  
Le module `demande_or` étend ainsi le périmètre du SIOR pour couvrir les besoins liés aux déménagements permanents, tout en préservant la compatibilité avec les flux d’OR temporaires existants.
