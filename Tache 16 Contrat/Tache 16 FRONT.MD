# Documentation technique – Frontend OR Dématérialisé → Visualisation Contrat

### (Suite logique du document Backend OR Dématérialisé → Création Contrat)

---

## Objectif

Décrire la **partie frontend Angular** permettant :

* d’afficher dans le back-office la liste des contrats OR
* d’inclure les **contrats issus d’une demande OR dématérialisée**
* de consulter le détail d’un contrat OR en ligne
* d’afficher un **statut cohérent avec le workflow OR**

la documentation couvre :

* les services Angular
* les modèles utilisés
* le composant de tableau
* les filtres et statuts
* les points de vigilance rencontrés

---

## 1. Architecture frontend – Vue d’ensemble

Le frontend est développé en **Angular (JHipster)**.

### Éléments principaux

* **Service HTTP**

  * `TableauContratService`
* **Composant**

  * `TableauContratComponent`
* **Modèles**

  * `ITableauContrat`
  * `ITableauContratExport`
* **Enum**

  * `Statut`
* **Vue**

  * `tableau-contrat.component.html`

---

## 2. Appel backend – Récupération des contrats

### Endpoint consommé

```http
GET /api/contrats
```

### Service Angular

```ts
query(req?: any): Observable<EntityArrayResponseTypeTableauContrat> {
  const options = createRequestOption(req);
  return this.http.get<Page<ITableauContrat>>(this.resourceUrl, {
    params: options,
    observe: 'response'
  });
}
```

### Rôle du service

* récupérer tous les contrats (OR bureau + OR dématérialisés)
* gérer la pagination
* transmettre les critères de filtre au backend

**Aucun filtrage spécifique n’exclut les contrats `DEMANDE_EN_LIGNE`**

---

## 3. Modèle ITableauContrat

Le modèle utilisé pour alimenter la table est volontairement **plat** (DTO de lecture).

```ts
export interface ITableauContrat {
  id?: number;
  offre?: string;
  numContrat?: string;
  personne?: string;
  adresseDepart?: string;
  adresseFinale?: string;
  agenceGestionnaire?: string;
  dateDebut?: string;
  dateFin?: string;
  statut?: Statut;
  orDisabled?: boolean;
}
```

### Points importants

* le champ `offre` est **optionnel**
* le champ `statut` est directement mappé depuis le backend
* `orDisabled` permet de désactiver certaines actions selon le workflow

---

## 4. Enum Statut côté frontend

Afin d’assurer une **parité stricte avec le backend**, l’énumération a été étendue :

```ts
export enum Statut {
  CREE = 'CREE',
  PREPARE = 'PREPARE',
  MODIFIE = 'MODIFIE',
  EN_COURS = 'EN_COURS',
  DEMANDE_EN_LIGNE = 'DEMANDE_EN_LIGNE',
  ECHU = 'ECHU',
  RESILIE = 'RESILIE',
}
```

### Importance

- évite les statuts inconnus dans le template
- permet un filtrage correct
- garantit un affichage cohérent du workflow OR

---

## 5. Affichage dans la table des contrats

### Colonne statut

```html
<td>
  {{ formatStatut(contrat.statut) }}
</td>
```

### Résultat attendu

| Valeur backend     | Affichage        |
| ------------------ | ---------------- |
| `DEMANDE_EN_LIGNE` | Demande en ligne |
| `CREE`             | Créé             |
| `EN_COURS`         | En cours         |

---

## 6. Accès au détail d’un contrat OR

Chaque ligne propose un lien vers la fiche contrat :

```html
<a [routerLink]="['/contrat', contrat.id, 'view']">
  {{ contrat.numContrat }}
</a>
```

### Comportement

* fonctionne pour les contrats OR bureau
* fonctionne pour les contrats OR dématérialisés
* aucune distinction technique côté front

**La consultation est unifiée**

---

## 7. Filtres & sélection

### Filtres disponibles

* offre
* dates (début / fin)
* personne
* adresses
* tournée facteur
* agence gestionnaire
* numéro de contrat
* statut (multiselect)

### Filtre sur les statuts

```html
<ng-multiselect-dropdown
  [(ngModel)]="filters.statut"
  [data]="allStatuts"
  (onChange)="applyFilter()">
</ng-multiselect-dropdown>
```

Le statut `DEMANDE_EN_LIGNE` est maintenant **filtrable et visible**.

---

## 8. Problème rencontré : affichage vide de la table

### Symptôme

* appel `/api/contrats` → HTTP 500
* aucune donnée affichée côté front
* console Angular vide

---

### Analyse

Les logs backend indiquaient :

```
Cannot invoke "Offre.getLibelleCourt()" because "offre" is null
```

Côté frontend, **l’erreur était invisible**, car elle survenait **avant la réponse HTTP**.

---

### Cause réelle (backend)

Les contrats OR dématérialisés :

* n’ont pas encore de forfait/offre
* mais le frontend attendait systématiquement un champ `offre`

Le mapping backend n’était pas **null-safe**

---

### Conséquence frontend

* Angular ne recevait aucune donnée exploitable
* la table restait vide sans message explicite

---

### Résolution

Rien à corriger côté frontend 
La correction a été faite **au niveau backend / formatter**, rendant l’API robuste face aux contrats OR web.

---

## 9. Cohérence avec les contrats OR bureau

| Élément        | OR Bureau         | OR Démat           |
| -------------- | ----------------- | ------------------ |
| Endpoint       | `/api/contrats`   | `/api/contrats`    |
| Modèle         | `ITableauContrat` | `ITableauContrat`  |
| Statut initial | `CREE`            | `DEMANDE_EN_LIGNE` |
| Consultation   | ✅                 | ✅                  |
| Filtres        | ✅                 | ✅                  |

**Aucune régression fonctionnelle**

---

## Conclusion

Le frontend OR :

* consomme l’API contrat de manière générique
* affiche les contrats OR dématérialisés sans logique spécifique
* gère correctement le nouveau statut `DEMANDE_EN_LIGNE`
* permet la consultation complète du contrat
* reste compatible avec l’existant (OR bureau)

L’intégration est **fluide**, **sans duplication**, et **alignée avec le backend**.

