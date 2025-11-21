
---
# **OR – Mise en place du premier formulaire OR (US OR-374)**

## Documentation technique — Implémentation & fonctionnement

---

# **Objectif de la User Story**

Permettre à un utilisateur d’accéder à une première version du **formulaire OR définitif**, qui contient :

* Une structure propre (routing + module + composant)
* Un affichage sans erreur
* Une première section de champs (demandeur / adresse)
* Reactive Forms fonctionnel
* Une validation minimale
* Une soumission qui affiche les données (pas encore d’appel backend)

---

# **Création du composant du formulaire**

Commande utilisée :

```bash
ng g component formulaire-or
```

Cela crée :

```
formulaire-or/
  formulaire-or.component.ts
  formulaire-or.component.html
  formulaire-or.component.scss
  formulaire-or.component.spec.ts
```

À ce stade, **pas de module associé**, ce qui crée des erreurs lorsque nous essayons d’importer les ReactiveForms.

---

#**Problème initial : ReactiveFormsModule ne s’importait pas**

JHipster organise son Angular en *modules feature*.
Comme ton composant n’avait **aucun module dédié**, Angular ne savait pas où charger :

* `ReactiveFormsModule`
* `FormsModule`

Cela causait :

```
NG8002: Can't bind to 'formGroup'
```

---

# **Solution : Création d’un module dédié au formulaire**

Nous avons créé un fichier :

`src/main/webapp/app/formulaire-or/formulaire-or.module.ts`

```ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { FormulaireOrComponent } from './formulaire-or.component';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild([
      {
        path: '',
        component: FormulaireOrComponent,
        data: { pageTitle: 'siorApp.formulaireOr.title' }
      }
    ])
  ],
  declarations: [FormulaireOrComponent],
})
export class FormulaireOrModule {}
```

Points clés :

✔ Le module importe correctement ReactiveFormsModule
✔ Le module gère son propre routing
✔ Aucun impact sur les autres composants JHipster

---

# **Ajout du routing principal (app-routing.module.ts)**

Dans le routing global, nous avons ajouté une entrée *lazy-loaded* :

```ts
{
  path: 'formulaire-or',
  loadChildren: () =>
    import('./formulaire-or/formulaire-or.module').then(m => m.FormulaireOrModule),
}
```

Avantages :

* Le module se charge uniquement quand on visite l’URL
* Aucun risque de casser d’autres modules JHipster
* Importation isolée

---

# **Mise en place du i18n**

Ajout du fichier :

`src/main/webapp/i18n/fr/Formulaire-Or.json`

Contenu adapté :

```json
{
  "siorApp": {
    "formulaireOr": {
      "title": "Formulaire OR",
      "demandeur": {
        "title": "Informations du demandeur",
        "nom": "Nom",
        "prenom": "Prénom",
        "email": "Adresse email"
      },
      "adresse": {
        "title": "Adresse du demandeur"
      },
      "actions": {
        "valider": "Valider"
      }
    }
  }
}
```

Et ajout dans `global.json` si nécessaire.

---

# **Implémentation du composant TS**

`formulaire-or.component.ts`

```ts
import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-formulaire-or',
  templateUrl: './formulaire-or.component.html',
})
export class FormulaireOrComponent {

  constructor(private fb: FormBuilder) {}

  formulaireOr = this.fb.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    adresse: ['', Validators.required],
  });

  onSubmit(): void {
    console.log("📨 Données du formulaire :", this.formulaireOr.value);
  }
}
```

Fonctionnalités :

✔ ReactiveForm complet
✔ Validation minimale
✔ Log en console lors de la soumission

---

# **Implémentation du HTML**

`formulaire-or.component.html`

```html
<div class="container mt-4">

  <h1 jhiTranslate="siorApp.formulaireOr.title"></h1>

  <form [formGroup]="formulaireOr" (ngSubmit)="onSubmit()" class="mt-3">

    <div class="form-group mb-3">
      <label jhiTranslate="siorApp.formulaireOr.demandeur.nom"></label>
      <input class="form-control" formControlName="nom" />
    </div>

    <div class="form-group mb-3">
      <label jhiTranslate="siorApp.formulaireOr.demandeur.prenom"></label>
      <input class="form-control" formControlName="prenom" />
    </div>

    <div class="form-group mb-3">
      <label jhiTranslate="siorApp.formulaireOr.demandeur.email"></label>
      <input class="form-control" formControlName="email" />
    </div>

    <div class="form-group mb-3">
      <label jhiTranslate="siorApp.formulaireOr.adresse.title"></label>
      <input class="form-control" formControlName="adresse" />
    </div>

    <button type="submit" class="btn btn-primary mt-3"
            [disabled]="formulaireOr.invalid">
      <span jhiTranslate="siorApp.formulaireOr.actions.valider"></span>
    </button>

  </form>

</div>
```

✔ Champs obligatoires
✔ Formulaire visible
✔ Validation Angular
✔ Bouton désactivé tant que non valide
✔ Aucun appel backend (phase 1 OK)


# **Conclusion**

Nous disposons maintenant :

✔ d’un **module indépendant et propre**
✔ d’un **routing propre JHipster**
✔ d’un **formulaire OR fonctionnel en Reactive Forms**
✔ d’un **i18n intégré**
✔ d’une **base solide pour étendre le formulaire**

La prochaine US pourra enrichir :

* les adresses complexes
* les options
* les dates
* l’intégration backend

---

