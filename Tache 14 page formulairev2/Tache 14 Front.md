# Documentation technique – Frontend OR Physique → Physique

### (Suite du document Backend)

## Objectif

Mettre en place l’interface Angular permettant :

* de saisir une demande d’Ordre de Réexpédition (OR)
* de rechercher une adresse via **REFloc**
* de sélectionner les adresses *Ancienne* et *Nouvelle*
* de valider dynamiquement les champs
* de soumettre les données au backend

Ce document décrit la **conception**, **l’architecture**, **les composants**, et les **choix techniques** réalisés.

---

# 1. Architecture front – Vue d’ensemble

Le front est développé en **Angular + JHipster**, avec :

* Composant principal : `FormulaireOrComponent`
* Services :

  * `OrDematService` → envoi de la demande au backend
  * `ReflocService` → recherche d’adresses
* Formulaire piloté via **FormGroup + FormControl**
* Validation en temps réel côté Angular
* Autocomplétion REFloc

Schéma logique simplifié :

```
Utilisateur → Formulaire Angular → ReflocService → Backend /api/refloc → SIG
                                     ↓
                          Données sélectionnées → Patch du FormGroup
                                     ↓
                               Envoi final → /api/demat/or-physique
```

---

# 2. Structure du formulaire Angular

Le composant `FormulaireOrComponent` expose un **FormGroup complet** :

```ts
this.formulaireOr = this.fb.group({
  dateDebut: ['', Validators.required],
  dateFin: ['', Validators.required],

  demandeurNom: ['', Validators.required],
  demandeurPrenom: ['', Validators.required],
  demandeurEmail: ['', [Validators.required, Validators.email]],
  demandeurTelephone: [''],

  // Champs spécifiques au REFloc (barres de recherche)
  rechercheAncienne: [''],
  rechercheNouvelle: [''],

  ancienneAdresse: this.fb.group({
    numEtVoie: ['', Validators.required],
    ville: ['', Validators.required],
    codePostal: ['', Validators.required],
  }),

  nouvelleAdresse: this.fb.group({
    numEtVoie: ['', Validators.required],
    ville: ['', Validators.required],
    codePostal: ['', Validators.required],
  }),
});
```

### Points importants :

* Les champs `rechercheAncienne` et `rechercheNouvelle` sont **hors** des FormGroup `ancienneAdresse` / `nouvelleAdresse`.
* Les données REFloc sélectionnées sont patchées automatiquement dans les sous-groupes.

---

# 3. Composant HTML – affichage & interactions

Chaque zone adresse contient :

1. **Un champ de recherche REFloc**
2. **Une liste dynamique de suggestions**
3. **Les champs renseignés après sélection**

Ex. pour l'ancienne adresse :

```html
<!-- Champ de recherche REFloc -->
<input type="text" class="form-control"
       formControlName="rechercheAncienne"
       (input)="searchReflocAncienne($any($event.target).value)"
       placeholder="Commencez à taper une adresse..." />

<!-- Suggestions -->
<ul class="list-group mt-1" *ngIf="suggestionsAncienne?.length">
  <li class="list-group-item list-group-item-action"
      *ngFor="let adresse of suggestionsAncienne"
      (click)="selectAncienneAdresse(adresse)">
    {{ adresse.display }}
  </li>
</ul>

<!-- Groupe ancienAdresse -->
<div formGroupName="ancienneAdresse">
  <input formControlName="numEtVoie" />
  <input formControlName="ville" />
  <input formControlName="codePostal" />
</div>
```

---

# 4. Service REFloc – Intégration côté frontend

Le service Angular **ne contacte pas SIG directement**.

Il interroge l’endpoint backend :

```
POST /api/refloc
```

### Code complet du service :

```ts
search(query: string, typeAdresse: string = "ADRESSE_DOMICILE_NC") {
  if (!query || query.length < 3) return of([]);

  const body = { q: query, typeAdresse };

  return this.http.post<any[]>(this.API_URL, body).pipe(
    map(items =>
      items.map(item => ({
        display: item.adresseFormatee?.replace('\n', ' ') 
             || `${item.numEtVoie} ${item.codePostal} ${item.ville}`,
        numEtVoie: item.numEtVoie,
        ville: item.ville,
        codePostal: item.codePostal
      }))
    )
  );
}
```

### Choix techniques :

* mapping simplifié pour le front
* suppression des retours à la ligne
* fallback si `adresseFormatee` n’est pas fournie

---

# 5. Sélection d’une adresse REFloc

Lorsqu’une suggestion est cliquée, elle est automatiquement réinjectée dans le formulaire :

```ts
selectAncienneAdresse(adresse: any): void {
  this.formulaireOr.patchValue({
    ancienneAdresse: {
      numEtVoie: adresse.numEtVoie,
      ville: adresse.ville,
      codePostal: adresse.codePostal
    },
    rechercheAncienne: ''  // efface la barre de recherche
  });

  this.suggestionsAncienne = [];
}
```

### Fonctionnalités :

✔ Remplissage automatique des champs
✔ Nettoyage de la zone de recherche
✔ Fermeture de la liste de suggestions

---

# 6. Impasses rencontrées (résolution des bugs)

Voici les principales difficultés rencontrées et leurs solutions :

### ❌ **CORS & accès SIG refusé**

➡ aucun appel direct SIG possible depuis Angular
✔ solution : passer par le backend `/api/refloc`

### ❌ **401 Unauthorized sur le backend**

✔ solution : connexion via Angular → JWT automatique par JHipster

### ❌ **Suggestions REFloc affichées en lignes blanches**

✔ cause : mauvais mapping des champs (`voie`, `postal`, etc.)
✔ solution : utiliser `adresseFormatee`, `numEtVoie`, `ville`, `codePostal`

### ❌ **PatchValue ne remplissait rien**

✔ solution : utiliser les bons champs du résultat REFloc

### ❌ **Barre de recherche impossible à vider**

✔ solution : ajouter formControlName `rechercheAncienne` / `rechercheNouvelle`

### ❌ **Erreur Angular : Cannot find control with path**

✔ cause : input de recherche placé dans `formGroupName`
✔ solution : déplacer le champ en dehors du FormGroup

Tous ces correctifs ont permis d’obtenir un formulaire stable et fonctionnel.

---

# 7. Soumission finale au backend

Lors du clic sur “Valider”, le composant construit le payload final :

```ts
const payload = {
  ...formValue,
  dateDebut: formValue.dateDebut + "T00:00:00Z",
  dateFin: formValue.dateFin + "T00:00:00Z"
};
```

Puis envoie à l’endpoint :

```
POST /api/demat/or-physique
```

Le backend valide :

* dates cohérentes
* adresses différentes
* présence des champs obligatoires

Et enregistre la demande.

---

# 8. Fonctionnalités front finalisées

✔ Recherche REFloc
✔ Autocomplétion dynamique
✔ Patch automatique des adresses
✔ Formulaire réactif complet
✔ Validations Angular + backend
✔ Gestion propre de l’affichage

---
# **Conclusion**

Le frontend OR Physique → Physique est désormais entièrement fonctionnel :
connexion avec le backend, gestion FORM + REFloc, validation, sélection d’adresses et envoi final.

Cette conception garantit une UX fluide, cohérente avec les règles métiers, et parfaitement alignée avec l’applicatif SIOR.


