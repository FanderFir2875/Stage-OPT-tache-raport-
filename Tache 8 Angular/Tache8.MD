# Tâche Front-End – Création de la page Angular “Demande OR”

## 🎯 Objectif général

Cette tâche vise à **comprendre et implémenter** le fonctionnement complet d’une nouvelle page Angular, intitulée **“Demande OR”**, dans le cadre du projet **SIOR**.
Elle a pour but de :

* créer un **composant Angular** (une page indépendante),
* le relier au **système de routage** (navigation entre pages),
* et l’intégrer dans la **barre de navigation principale** du site (navbar).

Même si le contenu de la page est encore vide (simple titre), cette étape valide la bonne **intégration technique du front-end** et prépare la suite (formulaire d’OR définitif).

---

## 🧠 Compréhension du contexte

Dans un projet Angular :

* Une **page** correspond à un **composant** (`Component`) → c’est un ensemble de fichiers (HTML, TS, SCSS).
* Une **route** (`Route`) relie une **URL** à un **composant**.
* La **navbar** agit comme un **menu de navigation** permettant à l’utilisateur d’aller d’une page à l’autre sans recharger toute l’application.

L’ensemble fonctionne grâce au **Router d’Angular**, qui gère la navigation côté client (single-page application).

---

## 🛠️ Étapes de réalisation et explication détaillée

### 1️⃣ Création du composant Angular

Commande exécutée :

```bash
ng generate component demande-or
```

👉 Cette commande crée automatiquement les fichiers suivants :

```
demande-or/
 ├── demande-or.component.ts        → logique du composant
 ├── demande-or.component.html      → structure HTML (vue)
 ├── demande-or.component.scss      → style spécifique à ce composant
 └── demande-or.component.spec.ts   → test unitaire du composant
```

### Explication :

* Le **`.ts`** contient la classe du composant et son annotation `@Component`.
* Le **`.html`** définit la vue qui sera affichée dans le navigateur.
* Le **`.scss`** gère le style local à ce composant.
* Le **`.spec.ts`** est le fichier de test Angular généré par défaut.

Exemple minimal de code HTML :

```html
<h1 style="text-align:center;">Demande OR</h1>
```

Cela permet de **vérifier visuellement** que la page s’affiche bien sans erreur avant d’ajouter du contenu plus complexe.

---

### 2️⃣ Configuration du routing Angular

Le **routing** est la clé du fonctionnement d’une SPA (Single Page Application).
Il permet de charger dynamiquement le bon composant selon l’URL, **sans recharger la page complète**.

Modification dans `SiorAppRoutingModule` :

```typescript
import { DemandeOrComponent } from './demande-or/demande-or.component';

RouterModule.forRoot(
  [
    {
      path: 'demande-or',
      component: DemandeOrComponent,
    },
    // autres routes existantes...
  ],
  { enableTracing: DEBUG_INFO_ENABLED }
)
```

### Explication :

* `path: 'demande-or'` : correspond au segment de l’URL (`/demande-or`).
* `component: DemandeOrComponent` : indique quelle page charger.
* `{ enableTracing: DEBUG_INFO_ENABLED }` : active un suivi dans la console pour déboguer les routes.

Résultat attendu :
👉 Naviguer vers `http://localhost:9000/demande-or` affiche directement la page “Demande OR”.

---

### 3️⃣ Intégration dans la barre de navigation (Navbar)

Ajout du lien dans `navbar.component.html` :

```html
<li class="nav-item" routerLinkActive="active">
  <a class="nav-link" routerLink="/demande-or">
    <fa-icon icon="envelope"></fa-icon>
    <span>Demande OR</span>
  </a>
</li>
```

### Explication :

* `<a routerLink="/demande-or">` : indique à Angular d’utiliser **le routeur interne** (et non une redirection serveur).
* `routerLinkActive="active"` : applique la classe CSS `active` au lien quand on est sur la page.
* L’icône `<fa-icon>` provient de **FontAwesome** (déjà intégré via JHipster).
* La condition `*ngIf="isAuthenticated()"` empêche l’affichage du lien si l’utilisateur n’est pas connecté (sécurité front).

---

### 4️⃣ Vérification et exécution du front-end

#### Démarrage en local :

```bash
npm start          # Lance Angular en mode développement (port 9000)
./gradlew bootRun  # Lance le backend Spring Boot (port 8080)
```

**Fonctionnement des ports :**

| Serveur               | Port        | Rôle                              |
| --------------------- | ----------- | --------------------------------- |
| Angular (front)       | 4200 / 9000 | Interface utilisateur             |
| Spring Boot (backend) | 8080        | API REST (traitements et données) |

Angular utilise un **proxy** vers `localhost:8080` pour communiquer avec le backend.

#### Vérifications effectuées :

* [x] Le lien “Demande OR” apparaît dans la barre de navigation.
* [x] Le clic affiche la page `Demande OR` sans rechargement de l’application.
* [x] Le serveur front (`npm start`) et back (`bootRun`) démarrent sans erreur.
* [x] Le build `npm run webapp:build` compile correctement le projet.

---

## ✅ Résultat obtenu

* La page **“Demande OR”** est maintenant intégrée dans le front Angular.
* La **navigation entre pages** via la navbar fonctionne.
* L’environnement Angular ↔ Spring Boot est configuré et opérationnel.
* Cette base servira à intégrer le **formulaire complet d’OR définitif** dans les prochaines étapes.

---

## 🧩 Structure finale du code

```
src/
└── main/
    └── webapp/
        ├── app/
        │   ├── demande-or/
        │   │   ├── demande-or.component.html
        │   │   ├── demande-or.component.scss
        │   │   ├── demande-or.component.ts
        │   │   └── demande-or.component.spec.ts
        │   └── app-routing.module.ts     ← route ajoutée ici
        └── layouts/
            └── navbar/
                └── navbar.component.html ← lien ajouté ici
```

---

## 🧠 Synthèse pédagogique – Comment ça marche

| Élément               | Fonction dans Angular                                | Équivalent côté backend                         |
| --------------------- | ---------------------------------------------------- | ----------------------------------------------- |
| **Component**         | Représente une “page” ou un bloc visuel              | Un `Controller` qui renvoie une vue             |
| **Routing**           | Définit les URL disponibles dans l’app               | Les routes Spring `@GetMapping`                 |
| **Navbar**            | Menu pour naviguer entre les composants              | Les liens ou menus Thymeleaf dans un projet MVC |
| **Service** (à venir) | Gestion de la logique métier côté front (appels API) | `Service` Spring Boot                           |
| **npm start**         | Lance Angular et BrowserSync                         | `./gradlew bootRun` pour le back                |
| **Hot Reload**        | Recharge automatique les changements                 | Auto-reload de Spring DevTools                  |

---

## 📘 Points clés à retenir

* Angular est une **SPA (Single Page Application)** : une seule page HTML est chargée, le reste est rendu dynamiquement.
* Les **routes Angular** remplacent les redirections classiques côté serveur.
* Les **composants** sont la base de la structure : un composant = une section de l’interface.
* La **navbar** centralise la navigation entre composants.
* JHipster configure déjà la liaison entre Angular (port 9000) et Spring Boot (port 8080).
* Le **cycle de développement** complet se teste avec `npm start` (front) et `bootRun` (back).

---

## 📅 Notes techniques et prochaines étapes

* **Version Angular :** 15 (via JHipster)
* **Backend :** Spring Boot 3 – Java 17
* **Authentification :** Obligatoire pour afficher la navbar et accéder à la page.
* **Proxy configuré :** `/api/**` → `localhost:8080`

### Prochaines étapes :

* Ajouter le **formulaire de création d’Ordre de Réexpédition définitif**.
* Intégrer la logique du **service Angular** pour appeler les endpoints `/api/or-definitif` et `/api/or-demat`.
* Créer les **DTO Angular** correspondants (`OrDematDTO`, `OrDematResponseDTO`).

---

✍️ **Auteur :** A. Nguyen
📅 **Date :** 06/11/2025
🔗 **Module concerné :** Front-End – SIOR (Gestion des OR dématérialisés)
📚 **But pédagogique :** Comprendre comment Angular structure ses pages, routes et composants pour préparer l’intégration complète du module “Demande OR”.
