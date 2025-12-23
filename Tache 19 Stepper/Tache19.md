# Documentation technique – Intégration du Stepper

## Formulaire OR dématérialisé (Angular / JHipster)

---

## Objectif de la modification

L’objectif était de **faire évoluer un formulaire Angular existant** afin de :

* découper la saisie en **plusieurs étapes**
* éviter d’afficher tous les champs simultanément
* améliorer l’ergonomie et la lisibilité
* forcer une **validation progressive** des données

La solution retenue est l’intégration du **Stepper Angular Material** (`mat-stepper`) autour du formulaire réactif existant, **sans remettre en cause sa structure fonctionnelle**.

---

## 1. Principe général d’intégration

Le formulaire existant reposait déjà sur un **Reactive Form (`FormGroup`)** avec des sous-groupes logiques :

* `demandeur`
* `ancienneAdresse`
* `nouvelleAdresse`

Cette structure était **parfaitement compatible** avec un stepper.

Le travail a donc consisté à :

* **encercler le formulaire par un `mat-stepper`**
* associer chaque sous-`FormGroup` à une étape
* bloquer la navigation tant que l’étape courante est invalide

---

## 2. Ajout du Stepper Angular Material

### Import des dépendances

Dans le module du formulaire :

```ts
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatStepperModule } from '@angular/material/stepper';
import { MatButtonModule } from '@angular/material/button';
```

Ces modules sont nécessaires pour :

* l’animation du stepper
* la navigation entre les étapes
* les boutons `Suivant / Précédent`

---

## 3. Encapsulation du formulaire dans un `mat-stepper`

Le formulaire existant a été encapsulé comme suit :

```html
<form [formGroup]="formulaireOr" (ngSubmit)="onSubmit()">

  <mat-stepper linear #stepper>

    <!-- Étapes ici -->

  </mat-stepper>

</form>
```

Le mode `linear` impose que :

* chaque étape soit valide
* avant de pouvoir accéder à la suivante

---

## 4. Association d’un FormGroup à une étape

Chaque étape (`mat-step`) est liée à un **sous-FormGroup précis** via `stepControl`.

### Exemple – Étape Demandeur

```html
<mat-step [stepControl]="demandeurForm">
  <ng-template matStepLabel>Demandeur</ng-template>

  <div formGroupName="demandeur">
    <!-- champs existants -->
  </div>
</mat-step>
```

Correspondance côté TypeScript :

```ts
get demandeurForm(): FormGroup {
  return this.formulaireOr.get('demandeur') as FormGroup;
}
```

Le stepper s’appuie directement sur la **validité Angular du FormGroup**.

---

## 5. Navigation contrôlée entre les étapes

Au lieu d’utiliser `matStepperNext` automatiquement, la navigation est déclenchée manuellement afin de :

* marquer les champs comme touchés
* afficher les erreurs
* empêcher toute progression invalide

```html
<button
  type="button"
  (click)="demandeurForm.markAllAsTouched(); stepper.next()"
  [disabled]="demandeurForm.invalid">
  Suivant
</button>
```

Même logique pour l’étape suivante :

```html
<button
  type="button"
  (click)="ancienneAdresseForm.markAllAsTouched(); stepper.next()"
  [disabled]="ancienneAdresseForm.invalid">
  Suivant
</button>
```

---

## 6. Gestion du retour arrière

La navigation inverse est assurée par Angular Material :

```html
<button type="button" matStepperPrevious>
  Précédent
</button>
```

Cette action :

* ne modifie pas l’état du formulaire
* ne réinitialise pas les valeurs saisies

---

## 7. Validation globale conservée

Le validateur global du formulaire (adresses différentes) a été conservé **au niveau du FormGroup racine** :

```ts
this.formulaireOr = this.fb.group(
  {
    demandeur: ...,
    ancienneAdresse: ...,
    nouvelleAdresse: ...
  },
  {
    validators: adressesDifferentesValidator,
  }
);
```

Le message d’erreur est affiché **uniquement à la dernière étape**, lorsque toutes les données sont présentes.

---

## 8. Soumission finale et reset du Stepper

Lors de la soumission finale :

```ts
next: () => {
  this.resetForm();
  this.stepper.reset();
}
```

Effets obtenus :

* retour automatique à la **première étape**
* formulaire entièrement réinitialisé
* expérience utilisateur fluide après création de l’OR

---

## 9. Problème rencontré – Styles de validation

### Symptôme

Après l’intégration du stepper :

* apparition de barres rouges verticales
* styles appliqués sur les conteneurs `formGroupName`

### Analyse

Inspection DOM :

```html
<div formgroupname="demandeur"
     class="ng-invalid ng-pristine">
```

Le style était appliqué **au niveau du FormGroup**, pas des champs.

### Correction

Neutralisation ciblée :

```scss
[formgroupname].ng-invalid,
[formgroupname].ng-touched {
  border-left: none !important;
}
```

Cette correction :

* supprime les barres visuelles parasites
* conserve les messages d’erreur
* est particulièrement adaptée à un formulaire multi-étapes

---

## Conclusion

L’intégration du stepper a été réalisée :

* **sans refonte du formulaire**
* en capitalisant sur la structure existante des FormGroup
* avec une validation progressive et cohérente
* tout en améliorant fortement l’UX

Le formulaire est désormais :

* plus lisible
* plus guidé
* mieux adapté à une saisie longue
* prêt pour des évolutions futures (récapitulatif, sous-composants, etc.)

---

Si tu veux, je peux aussi te faire :

* une **version ultra courte pour un commit message**
* ou une **version orientée Confluence** (plus synthétique)
