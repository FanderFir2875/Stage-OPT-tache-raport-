# Tests d’intégration – Ordres de Réexpédition (OR)

## Objectif de ces tests

Cette classe de tests vérifie le **bon fonctionnement des endpoints REST** liés aux **demandes d’Ordres de Réexpédition (OR)**, en particulier :

* la **soumission du formulaire OR physique**
* la **persistance correcte des données en base**
* la **consultation des demandes OR côté administration**

  * liste des demandes
  * détail d’une demande

Les tests sont des **tests d’intégration complets**, c’est-à-dire :

* l’application démarre réellement (Spring Boot)
* la base de données est utilisée
* les endpoints REST sont appelés via HTTP (MockMvc)

---

## Configuration générale du test

```java
@SpringBootTest(classes = SiorApp.class)
@AutoConfigureMockMvc
@Transactional
class OrDematResourceIT {
```

### Explication

| Élément                 | Rôle                                             |
| ----------------------- | ------------------------------------------------ |
| `@SpringBootTest`       | Démarre toute l’application Spring               |
| `@AutoConfigureMockMvc` | Permet d’appeler les endpoints REST              |
| `@Transactional`        | Annule les changements en base après chaque test |

Chaque test s’exécute **dans une transaction isolée** et ne pollue pas les autres tests.

---

## Nettoyage avant chaque test

```java
@BeforeEach
void initTest() {
    demandeOrRepository.deleteAll();
}
```



## Organisation des tests

Les tests sont regroupés avec une classe interne :

```java
@Nested
class OrPhysiqueResourceIT {
```

### Avantages pédagogiques

* Sépare clairement les **tests OR physique** du reste
* Meilleure lisibilité
* Structure évolutive si d’autres types de tests arrivent (OR démat, admin…)

---

## Test 1 — Soumission du formulaire OR

```java
@Test
void submitForm_shouldCreateDemandeOr()
```

### Ce que ce test vérifie

Lorsqu’un utilisateur :

* remplit le formulaire OR
* clique sur **Valider**

Alors :

* l’API accepte la demande
* la demande est persistée
* un message de confirmation est retourné

---

### Création d’un DTO valide

```java
OrDematRequestDTO dto = TestUtil.buildValidOrDematRequestDTO();
```

Pourquoi utiliser un **TestUtil** ?

* Évite la duplication de code
* Centralise les jeux de données valides
* Rend les tests plus lisibles

---

### Appel de l’endpoint REST

```java
mvc.perform(post("/api/demat/or-physique")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(dto)))
```

Ici :

* `MockMvc` simule un appel HTTP réel
* le DTO est envoyé en JSON
* aucun serveur n’est réellement démarré

---

### Vérification de la réponse

```java
.andExpect(status().isOk())
.andExpect(jsonPath("$.message")
    .value("Votre demande d’Ordre de Réexpédition a bien été enregistrée."));
```

On vérifie :

* le code HTTP
* le message utilisateur affiché côté front

---

### Vérification en base de données

```java
List<DemandeOr> all = demandeOrRepository.findAll();
assertThat(all).hasSize(1);
```

Puis :

```java
DemandeOr saved = all.get(0);
assertThat(saved.getDateDebut()).isNotNull();
assertThat(saved.getDateFin()).isNotNull();
assertThat(saved.getData()).isNotNull();
```

Objectif :

* s’assurer que **les données sont réellement persistées**
* éviter les faux positifs (API OK mais rien en base)

---

## Test 2 — Liste des demandes OR (admin)

```java
@Test
void listAll_shouldReturnOrList()
```

### Objectif

Vérifier que :

```http
GET /api/demat/or-demandes
```

retourne correctement une liste d’OR.

---

### Étapes

1️⃣ Création manuelle d’une demande OR

```java
DemandeOr or = new DemandeOr();
or.setDateDebut(...);
or.setDateFin(...);
or.setData(...);
```

👉 Ici on initialise directement l’entité pour le test.

---

2️⃣ Appel du endpoint

```java
mvc.perform(get("/api/demat/or-demandes"))
```

---

3️⃣ Vérification du résultat

```java
.andExpect(status().isOk())
.andExpect(jsonPath("$[0].demandeurNom").value("Jean"));
```

On vérifie que :

* la liste est retournée
* les données JSON sont bien mappées

---

## Test 3 — Détail d’une demande OR

```java
@Test
void getDetail_shouldReturnDemandeOrDetail()
```

### Objectif

Vérifier le fonctionnement de :

```http
GET /api/demat/or-demandes/{id}
```

---

### Étapes

1️⃣ Création d’une demande OR complète

```java
DemandeOr or = demandeOrRepository.saveAndFlush(
    TestUtil.buildDemandeOr()
);
```

---

2️⃣ Appel REST avec ID

```java
mvc.perform(get("/api/demat/or-demandes/{id}", or.getId()))
```

---

3️⃣ Vérifications

```java
.andExpect(jsonPath("$.demandeurNom").value("Dupont"))
.andExpect(jsonPath("$.ancienneAdresse.ville").value("Nouméa"));
```

- Le mapping JSON → DTO est correct
- Les données imbriquées sont bien exposées



