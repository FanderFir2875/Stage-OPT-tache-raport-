# Explication des changements – Formulaire OR avec pièces justificatives

Cette tâche avait pour objectif d’**ajouter la gestion des pièces justificatives** à la création d’une demande d’Ordre de Réexpédition (OR), tout en **restant logique fonctionnellement** et **simple pour l’utilisateur**.

---

## Changement clé de conception (IMPORTANT)

### Avant

* La demande OR était **finalisée à l’étape “Nouvelle adresse”**
* Les pièces justificatives étaient pensées comme une étape séparée
* Problème : **une pièce doit être liée à une demande existante**
* Donc impossible d’uploader avant d’avoir un `demandeOrId`

### Maintenant (choix final)

**La demande OR et la pièce justificative sont finalisées ensemble dans `onSubmit()`**

Cela garantit :

* un **flux simple et linéaire**
* un **ID de demande disponible**
* aucune gestion intermédiaire compliquée du stepper

---

## Nouveau flux fonctionnel

### Parcours utilisateur

1. L’utilisateur remplit :

   * Demandeur
   * Ancienne adresse
   * Nouvelle adresse
2. L’utilisateur sélectionne :

   * un **type de pièce justificative**
   * un **fichier**
3. Il clique sur **Valider**
4. Le système :

   1. valide le formulaire
   2. vérifie la présence d’une pièce justificative
   3. crée la demande OR
   4. uploade la pièce dans MinIO
   5. enregistre la pièce en base
   6. finalise la demande

---

## Changements détaillés dans le composant `FormulaireOrComponent`

---

## Ajout de l’état local pour la pièce justificative

```ts
selectedFile: File | null = null;
typePiece: string | null = null;
demandeOrId: number | null = null;
```

### Pourquoi ?

* Stocker temporairement la pièce choisie
* Ne **pas** l’envoyer tant que la demande OR n’existe pas
* Éviter tout upload prématuré

---

##  Gestion du fichier (sélection uniquement)

```ts
onFileSelected(event: Event): void {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    this.selectedFile = input.files[0];
  }
}
```

### Choix important

**Aucun upload ici**

* Cette méthode ne fait **que stocker le fichier**
* L’upload réel est déclenché **uniquement dans `onSubmit()`**
* Cela évite les incohérences et simplifie le flux

---

## Validation renforcée dans `onSubmit()`

### Validation du formulaire

```ts
if (this.formulaireOr.invalid) {
  this.formulaireOr.markAllAsTouched();
  return;
}
```

Empêche toute création si les champs obligatoires ne sont pas remplis

---

### Validation métier : pièce obligatoire

```ts
if (!this.selectedFile || !this.typePiece) {
  alert('Une pièce justificative est obligatoire.');
  return;
}
```

Règle métier claire :

* **une demande OR sans pièce justificative est invalide**

---

## Création de la demande OR (inchangé fonctionnellement)

```ts
this.orService.createOrDemat(payload).subscribe({
  next: (res: any) => {
    const demandeOrId = res.id;
```

* La demande est créée **avant toute interaction avec MinIO**
* L’ID retourné est la clé du reste du processus

---

## Upload de la pièce justificative après création

```ts
this.pieceService
  .upload(demandeOrId, this.selectedFile!, this.typePiece!)
```

### Pourquoi ici ?

* `demandeOrId` existe maintenant
* L’upload MinIO est **fiable**
* La pièce est correctement rattachée à la demande

---

## Finalisation complète de la demande

```ts
alert('Demande de réexpédition finalisée avec succès');
```

**La demande n’est considérée comme finalisée que lorsque :**

* la demande OR est créée
* la pièce est uploadée
* la pièce est enregistrée en base

---

## Nettoyage de l’état après succès

```ts
this.resetForm();
this.stepper.reset();
this.selectedFile = null;
this.typePiece = null;
```

### Objectifs

* Éviter les données résiduelles
* Permettre une nouvelle saisie propre
* Garantir une UX propre

---

## Améliorations structurelles apportées

### ✔ Suppression des uploads intermédiaires

* Plus de bouton “Ajouter la pièce”
* Une seule action finale : **Valider**

### ✔ Flux métier clair

* Une action utilisateur
* Une transaction logique
* Un état cohérent backend / frontend

### ✔ Code plus lisible

* `onSubmit()` devient le point central
* Séparation claire :

  * sélection du fichier
  * validation
  * création
  * upload

---

## Conclusion

Cette implémentation :

* respecte la **logique métier**
* évite les erreurs d’état
* garantit l’intégrité des données
* simplifie l’expérience utilisateur
* s’intègre naturellement avec MinIO

---

```ts
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatStepper } from '@angular/material/stepper';
import moment from 'moment';

import { OrService } from './formulaire-or.service';
import { ReflocService } from './refloc.service';
import { PieceJustificativeService } from 'app/shared/piece-justificative/piece-justificative.service';

@Component({
selector: 'jhi-app-formulaire-or',
templateUrl: './formulaire-or.component.html',
styleUrls: ['./formulaire-or.component.scss'],
})
export class FormulaireOrComponent implements OnInit {
formulaireOr!: FormGroup;

forfaits: any[] = [];
offreOrDefinitifId = 2;

suggestionsAncienne: any[] = [];
suggestionsNouvelle: any[] = [];

selectedFile: File | null = null;
typePiece: string | null = null;
demandeOrId: number | null = null;

@ViewChild(MatStepper) stepper!: MatStepper;

constructor(
    private fb: FormBuilder,
    private orService: OrService,
    private reflocService: ReflocService,
    private pieceService: PieceJustificativeService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadForfaits();
  }

  /* =====================
     GETTERS
  ===================== */

  get demandeurForm(): FormGroup {
    return this.formulaireOr.get('demandeur') as FormGroup;
  }

  get ancienneAdresseForm(): FormGroup {
    return this.formulaireOr.get('ancienneAdresse') as FormGroup;
  }

  get nouvelleAdresseForm(): FormGroup {
    return this.formulaireOr.get('nouvelleAdresse') as FormGroup;
  }

  /* =====================
     REFLOC
  ===================== */

  searchReflocAncienne(query: string): void {
    if (!query || query.length < 3) {
      this.suggestionsAncienne = [];
      return;
    }

    this.reflocService.search(query).subscribe(res => {
      this.suggestionsAncienne = res;
    });
  }

  selectAncienneAdresse(adresse: any): void {
    this.ancienneAdresseForm.patchValue(adresse);
    this.suggestionsAncienne = [];
  }

  searchReflocNouvelle(query: string): void {
    if (!query || query.length < 3) {
      this.suggestionsNouvelle = [];
      return;
    }

    this.reflocService.search(query).subscribe(res => {
      this.suggestionsNouvelle = res;
    });
  }

  selectNouvelleAdresse(adresse: any): void {
    this.nouvelleAdresseForm.patchValue(adresse);
    this.suggestionsNouvelle = [];
  }

  /* =====================
     FILE
  ===================== */

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  /* =====================
     STEP 3 : CREATION OR
  ===================== */

  onSubmit(): void {
    // 1️⃣ Validation formulaire
    if (this.formulaireOr.invalid) {
      this.formulaireOr.markAllAsTouched();
      return;
    }

    // 2️⃣ Validation pièce justificative
    if (!this.selectedFile || !this.typePiece) {
      alert('Une pièce justificative est obligatoire.');
      return;
    }

    const value = this.formulaireOr.value;

    // 3️⃣ Payload création OR
    const payload = {
      dateDebut: moment(value.demandeur.dateDebut).format('YYYY-MM-DD'),
      forfaitId: value.demandeur.forfaitId,
      demandeurNom: value.demandeur.nom,
      demandeurPrenom: value.demandeur.prenom,
      demandeurEmail: value.demandeur.email,
      demandeurTelephone: value.demandeur.telephone,
      ancienneAdresse: value.ancienneAdresse,
      nouvelleAdresse: value.nouvelleAdresse,
    };

    // 4️⃣ Création de la demande
    this.orService.createOrDemat(payload).subscribe({
      next: (res: any) => {
        const demandeOrId = res.id;

        if (!demandeOrId) {
          alert('Erreur lors de la création de la demande.');
          return;
        }

        // 5️⃣ Upload pièce justificative
        this.pieceService
          .upload(demandeOrId, this.selectedFile!, this.typePiece!)
          .subscribe({
            next: () => {
              alert('Demande de réexpédition finalisée avec succès');

              // 6️⃣ Reset
              this.resetForm();
              this.stepper.reset();
              this.selectedFile = null;
              this.typePiece = null;
            },
            error: () => {
              alert('Erreur lors de l’upload de la pièce justificative');
            },
          });
      },
      error: () => {
        alert('Erreur lors de la création de la demande');
      },
    });
  }




  /* =====================
     INIT / RESET
  ===================== */

  private initForm(): void {
    this.formulaireOr = this.fb.group(
      {
        demandeur: this.fb.group({
          dateDebut: ['', [Validators.required, dateDebutFutureOrTodayValidator]],
          forfaitId: [null, Validators.required],
          nom: ['', Validators.required],
          prenom: ['', Validators.required],
          email: ['', [Validators.required, Validators.email]],
          telephone: [''],
        }),
        ancienneAdresse: this.fb.group({
          recherche: [''], // ✅ UNE SEULE FOIS
          numEtVoie: ['', Validators.required],
          ville: ['', Validators.required],
          codePostal: ['', Validators.required],
        }),

        nouvelleAdresse: this.fb.group({
          recherche: [''], // ✅ UNE SEULE FOIS
          numEtVoie: ['', Validators.required],
          ville: ['', Validators.required],
          codePostal: ['', Validators.required],
        }),
      },
      { validators: adressesDifferentesValidator }
    );
  }

  private resetForm(): void {
    this.formulaireOr.reset();
    this.demandeOrId = null;
    this.selectedFile = null;
    this.typePiece = null;
  }

  private loadForfaits(): void {
    this.orService
      .getForfaitsByOffre(this.offreOrDefinitifId)
      .subscribe(res => (this.forfaits = res));
  }
}

/* =====================
   VALIDATORS
===================== */

export function dateDebutFutureOrTodayValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  return moment(control.value).isBefore(moment().startOf('day'))
    ? { dateDebutInvalide: true }
    : null;
}

export function adressesDifferentesValidator(control: AbstractControl): ValidationErrors | null {
  const a = control.get('ancienneAdresse')?.value;
  const n = control.get('nouvelleAdresse')?.value;
  if (!a || !n) return null;
  return a.numEtVoie === n.numEtVoie && a.ville === n.ville && a.codePostal === n.codePostal
    ? { sameAddress: true }
    : null;
}


```
