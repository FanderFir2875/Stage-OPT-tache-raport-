## Step 4 – Pièces justificatives

### Position dans le stepper

Le **Stepper 4** correspond à la **dernière étape du formulaire**.
Il a pour rôle de **finaliser la demande OR** en collectant **obligatoirement une pièce justificative** avant l’envoi définitif.

```html
<mat-step>
  <ng-template matStepLabel>Pièces justificatives</ng-template>
```

Contrairement aux étapes précédentes :

* il **ne possède pas de `stepControl`**
* la validation ne repose **pas sur un FormGroup**
* la validation est **métier**, gérée côté TypeScript

---

## Objectif fonctionnel du Step 4

Cette étape permet :

* de sélectionner le **type de pièce justificative**
* de sélectionner un **fichier (PDF / image)**
* de **bloquer la validation finale** si aucune pièce n’est fournie
* de garantir qu’une demande OR **ne peut pas exister sans justificatif**

---

## Sélection du type de pièce

```html
<select
  class="form-select"
  [(ngModel)]="typePiece"
  [ngModelOptions]="{ standalone: true }"
>
```

### Points importants :

* Utilisation de `ngModel` **hors Reactive Form**
* `standalone: true` est **obligatoire** pour :

  * éviter les conflits avec `formGroup`
  * ne pas rattacher ce champ au formulaire principal
* La valeur sélectionnée correspond à l’énumération backend :

  * `CARTE_IDENTITE`
  * `PASSEPORT`
  * `JUSTIFICATIF_DOMICILE`
  * `AUTRE`

Le type est stocké dans la variable `typePiece` du composant TS.

---

## Sélection du fichier

```html
<input
  type="file"
  class="form-control"
  (change)="onFileSelected($event)"
  accept=".pdf,.jpg,.jpeg,.png"
/>
```

### Comportement :

* L’utilisateur sélectionne un fichier local
* Le fichier est **uniquement stocké côté frontend**
* Aucun upload n’est déclenché ici
* Le fichier est conservé dans la variable `selectedFile`

Le vrai upload vers MinIO est fait **plus tard**, dans `onSubmit()`.

---

## Message d’avertissement (validation métier)

```html
<div
  class="alert alert-warning mt-3"
  *ngIf="!selectedFile || !typePiece"
>
  ⚠ Une pièce justificative est obligatoire pour finaliser la demande.
</div>
```

### Rôle de ce message :

* Indiquer clairement à l’utilisateur ce qui manque
* Fournir un retour visuel immédiat
* Ne pas bloquer la navigation, mais bloquer la validation finale

C’est une **validation UX**, complémentaire à la validation TS.

---

## Bouton de validation finale

```html
<button
  type="submit"
  class="btn btn-success"
  [disabled]="!selectedFile || !typePiece"
>
  Valider
</button>
```

### Points clés :

* Le bouton déclenche **`onSubmit()`**
* Il est **désactivé tant que** :

  * aucun fichier n’est sélectionné
  * aucun type de pièce n’est choisi
* Cela empêche toute soumission invalide côté frontend

---

## Navigation dans le stepper

```html
<button
  type="button"
  mat-button
  matStepperPrevious
  class="btn btn-secondary"
>
  Précédent
</button>
```

* Permet de revenir aux étapes précédentes
* N’affecte pas les données déjà saisies
* Conforme au comportement standard des autres étapes

---

## Lien avec le TypeScript

Le Step 4 **ne crée rien seul**.

Lors du clic sur **Valider** :

1. `onSubmit()` est appelé
2. Le formulaire est validé
3. La présence de la pièce est vérifiée
4. La demande OR est créée
5. La pièce justificative est uploadée vers MinIO
6. La demande est finalisée

Le Step 4 est donc **la clé de la finalisation complète**.

---

## Pourquoi ce choix d’implémentation ?

* ✔ Pas de création partielle de demande
* ✔ Pas d’upload sans ID de demande
* ✔ Flux clair et linéaire
* ✔ Validation métier explicite
* ✔ UX simple et compréhensible

---

## Conclusion – Stepper 4

Le Stepper 4 :

* centralise la **finalisation de la demande**
* impose la **présence d’une pièce justificative**
* déclenche le **processus complet backend + MinIO**
* garantit la cohérence fonctionnelle et technique

**C’est l’étape de clôture officielle de la demande OR.**

Si tu veux, au prochain message, je peux :

* reformuler tout ça en **Markdown final prêt Confluence**
* ou faire un **schéma de flux (frontend ↔ backend ↔ MinIO)**
