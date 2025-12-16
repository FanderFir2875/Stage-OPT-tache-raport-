## Implémentation FRONT-END – Détail Contrat Admin

## OR dématérialisé (OR définitif)

---

## Objectif côté front

Permettre à un **administrateur** de consulter le **détail complet d’un contrat OR créé en ligne**, afin de :

* Vérifier les informations avant traitement
* Identifier clairement l’origine **“Souscription en ligne”**
* Consulter toutes les données métier :

  * Offre / Forfait
  * Dates
  * Adresses
  * Données client
  * Vérification d’identité
* Garantir un affichage robuste (aucun champ vide)

---

## Principes de conception retenus

* Aucune logique métier côté front
* Aucune lecture du JSON `data`
* Le front s’appuie uniquement sur le **ContratComplet** exposé par le backend
* Tous les champs optionnels sont sécurisés avec **“Non renseigné”**
* Libellés métier affichés (pas de codes techniques)

---

## Écran concerné

* **Page admin – Détail du contrat**
* Composant Angular :

  * `ContratDetailComponent`
  * sous-composant : `ContratDetailAdresseComponent`

---

## Informations générales du contrat

### Affichages réalisés

Les informations suivantes sont affichées **en haut de page**, sans scroll :

* Numéro de contrat
  `Contrat n°202512-10`
* Date de création
* Créé par (agence)
* Dernière modification
* Statut du contrat
  `DEMANDE_EN_LIGNE`
* Origine du contrat

### Origine “Souscription en ligne”

Un bloc visuel dédié indique clairement :

> **Contrat créé via le formulaire en ligne**

Visible immédiatement
Conforme aux critères d’acceptation
Distinct des autres informations

---

## Offre / Forfait

### Comportement

Une section dédiée affiche :

* Offre
* Forfait

Si les données ne sont pas encore disponibles :

```text
Non renseigné
```

### Implémentation

Le template utilise directement :

```html
{{ contrat?.forfait?.offre?.libelle || 'Non renseigné' }}
{{ contrat?.forfait?.libelle || 'Non renseigné' }}
```

Aucun mapping intermédiaire
Aucune variable front “fantôme”

---

## Dates du contrat

### Champs affichés

* Date de début
* Date de fin

Les dates sont :

* fournies par le backend
* calculées automatiquement à partir du forfait
* affichées en lecture seule

- Aucun calcul côté front
- Cohérence garantie avec les règles métier

---

## Gestion des adresses (composant dédié)

### Composant

```ts
ContratDetailAdresseComponent
```

Responsabilités :

* afficher les adresses **initiale** et **de destination**
* gérer les cas BP / Domicile / Agence
* sécuriser tous les champs optionnels

---

## Adresse initiale

### Champs affichés

* Type d’adresse (BP / Domicile / Agence)
* Numéro de BP (si applicable)
* Code postal
* Ville
* Agence gestionnaire

### Choix d’implémentation

* Le **type d’adresse** est affiché via un libellé métier
* Les champs absents affichent **“Non renseigné”**
* L’affichage est conditionné par la domiciliation

Exemple :

```text
BP : 30490
Code postal : 98895
Ville : NOUMEA CEDEX
Agence gestionnaire : NEA_BELLE_VIE
```

---

## Adresse de destination

### Champs affichés

* **Type de destination** (champ explicite)
* Point de remise
* Complément
* Numéro et voie
* Lieu-dit
* Code postal
* Ville

### Type de destination (ajout clé)

Un champ dédié a été ajouté **en tête de section** :

```text
Type de destination : Domicile
```

Règle :

* si `pointDeRemise` est présent → **Point de remise**
* sinon → libellé issu du type d’adresse

- Évite toute ambiguïté métier
- Conforme aux critères d’acceptation

---

## Sécurisation des champs “Non renseigné”

### Méthode utilitaire

```ts
displayValue(value?: string | number | null): string {
  return value !== undefined && value !== null && `${value}`.trim() !== ''
    ? `${value}`
    : 'Non renseigné';
}
```

Utilisée pour :

* complément
* lieu-dit
* point de remise
* BP
* code postal
* ville

✔️ Aucun champ vide visible
✔️ Affichage robuste pour la recette

---

## Informations client

### Section Client

Affichage des informations suivantes :

* Type de client
   Personne / PHYSIQUE
* Mode de présentation
   “Le client s’est présenté en tant que TITULAIRE”
* Nom
* Prénom
* Téléphone
* Email

Toutes les informations sont :

* en lecture seule
* visibles sans action supplémentaire

---

## Vérification d’identité

Un indicateur visuel confirme :

```text
J’ai vérifié l’identité du client
```

- Conforme aux critères
- Lecture immédiate pour l’admin

---

## Résultat FRONT-END final

- Tous les critères d’acceptation respectés
- Aucun champ vide affiché
- Libellés compréhensibles métier
- Origine “Souscription en ligne” clairement identifiée
- Aucune dépendance au JSON brut
- Page prête pour validation PO / recette

---

## Conclusion

L’implémentation front complète et valorise le travail backend :

* les règles métier sont centralisées côté serveur
* le front se concentre sur l’affichage et la lisibilité
* l’admin dispose d’une vue fiable et exploitable du contrat OR dématérialisé

