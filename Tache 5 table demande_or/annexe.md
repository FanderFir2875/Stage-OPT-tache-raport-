# Annexe — Compréhension des couches Spring Boot & JPA

### (Application : SIOR – Module `DemandeOr`)

---

## 1. Architecture en couches : principe général

L’architecture Spring Boot repose sur le **principe de séparation des responsabilités** (“Separation of Concerns”).
Chaque couche a un rôle bien précis et ne doit pas en assumer un autre.

```text
+--------------------------------------------------------------+
|                  Frontend (Vue.js / Angular)                 |
+------------------------------↓-------------------------------+
|  Controller (Web / REST)  → Reçoit les requêtes HTTP         |
+------------------------------↓-------------------------------+
|  Service (Business)       → Contient la logique métier       |
+------------------------------↓-------------------------------+
|  Repository (Data Access) → Accès aux données via JPA        |
+------------------------------↓-------------------------------+
|  Database (PostgreSQL)    → Stockage persistant              |
+--------------------------------------------------------------+
```

---

## 2. Description détaillée des couches (avec exemple concret)

### **Entity (Domaine)**

> Représente une table de la base de données.

**Exemple :**

```java
@Entity
@Table(name = "demande_or")
public class DemandeOr extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_debut", nullable = false)
    private Instant dateDebut;

    @Column(name = "date_fin", nullable = false)
    private Instant dateFin;
}
```

🔹 Chaque instance de `DemandeOr` correspond à **une ligne** de la table `demande_or`.
🔹 Les champs sont directement mappés aux colonnes SQL.

---

### **Repository (DAO)**

> Fournit un accès simple aux entités via Spring Data JPA.

**Exemple :**

```java
@Repository
public interface DemandeOrRepository extends JpaRepository<DemandeOr, Long> {
}
```

💡 Grâce à `JpaRepository`, on dispose déjà de méthodes prêtes à l’emploi :

* `save(entity)` — insère ou met à jour
* `findAll()` — récupère toutes les lignes
* `findById(id)` — récupère une ligne spécifique
* `deleteById(id)` — supprime une ligne

---

### **Service (Business Logic)**

> Contient la logique métier, c’est-à-dire les règles du domaine.

**Exemple :**

```java
@Service
@Transactional
public class OrDematService {

    private final DemandeOrRepository demandeOrRepository;
    private final DemandeOrMapper demandeOrMapper;

    public OrDematService(DemandeOrRepository demandeOrRepository, DemandeOrMapper demandeOrMapper) {
        this.demandeOrRepository = demandeOrRepository;
        this.demandeOrMapper = demandeOrMapper;
    }

    public OrDematResponseDTO processOrDemat(OrDematDTO dto) {
        // 1. Validation des données
        // 2. Conversion DTO → Entité
        // 3. Persistance
        // 4. Retour d’une réponse propre
    }
}
```

🔸 Le service ne connaît ni le frontend ni la base.
Il orchestre simplement la logique métier (validation, conversion, enregistrement…).

---

### **Mapper (MapStruct)**

> Transforme les objets entre les couches sans code manuel.

**Exemple :**

```java
@Mapper(componentModel = "spring")
public interface DemandeOrMapper {
    @Mapping(target = "dateDebut", expression = "java(toInstant(dto.getDateDebut()))")
    @Mapping(target = "dateFin", expression = "java(toInstant(dto.getDateFin()))")
    DemandeOr toEntity(OrDematDTO dto);

    default Instant toInstant(String date) {
        return (date == null || date.isBlank()) ? null :
            LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
```

Avantages :

* Évite les erreurs de conversion manuelle.
* MapStruct génère automatiquement le code au moment de la compilation.
* Maintient une **parfaite séparation** entre les objets de transport (DTO) et les entités JPA.

---

### **Controller (REST API)**

> Interface entre le client (front) et la logique métier.

**Exemple :**

```java
@RestController
@RequestMapping("/api")
public class OrDematResource {

    private final OrDematService orDematService;

    public OrDematResource(OrDematService orDematService) {
        this.orDematService = orDematService;
    }

    @PostMapping("/or-demat")
    public ResponseEntity<?> createOrDemat(@Valid @RequestBody OrDematDTO dto) {
        try {
            OrDematResponseDTO response = orDematService.processOrDemat(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
```

Le Controller :

* Reçoit le JSON du frontend.
* Le transforme en `OrDematDTO`.
* Appelle le service.
* Retourne une réponse HTTP propre (`ResponseEntity` avec 201 ou 400).

---

## 3. Schéma de flux complet : du Frontend à la Base

```text
[ Frontend Vue.js ]
       |
       |  JSON (OrDematDTO)
       ↓
[ Controller : OrDematResource ]
       |
       |  Appel du service
       ↓
[ Service : OrDematService ]
       |
       |  Conversion DTO → Entité
       ↓
[ Mapper : DemandeOrMapper ]
       |
       |  Persistance en base
       ↓
[ Repository : DemandeOrRepository ]
       |
       |  SQL via Hibernate
       ↓
[ PostgreSQL : Table demande_or ]
```

---

## 4. Exemple concret de traçabilité de données

### 🔸 Requête HTTP (côté front)

```http
POST /api/or-demat
{
  "dateDebut": "2025-02-01",
  "dateFin": "2025-03-01"
}
```

### 🔸 Étapes exécutées

| Étape | Action                       | Classe impliquée         |
| ----- | ---------------------------- | ------------------------ |
| 1️⃣   | JSON reçu et désérialisé     | `OrDematResource`        |
| 2️⃣   | Validation des champs        | `OrDematService`         |
| 3️⃣   | Conversion vers entité       | `DemandeOrMapper`        |
| 4️⃣   | Persistance en base          | `DemandeOrRepository`    |
| 5️⃣   | Ajout de l’audit automatique | `AbstractAuditingEntity` |
| 6️⃣   | Réponse envoyée au front     | `OrDematResponseDTO`     |

---

## 5. Audit et logs d’application

### 🔹 Audit automatique

Chaque entité qui hérite d’`AbstractAuditingEntity` possède :

* `createdBy` → identifiant de l’utilisateur connecté
* `createdDate` → date et heure de création
* `lastModifiedBy` → dernier modificateur
* `lastModifiedDate` → date de modification

➡ Ces champs sont **automatiquement remplis** par le système Spring lors des opérations JPA (`save`, `update`).

### 🔹 Logs applicatifs

Les logs des appels REST et des validations se trouvent dans :

```
/logs/sior.log
```

Ils permettent de retracer :

* les entrées utilisateurs invalides,
* les créations réussies,
* les éventuelles exceptions métier (`IllegalArgumentException`).

---

## 6. Récapitulatif synthétique

| Couche         | Classe                   | Rôle                       | Exemple d’action           |
| -------------- | ------------------------ | -------------------------- | -------------------------- |
| **Controller** | `OrDematResource`        | Reçoit la requête HTTP     | `/api/or-demat`            |
| **Service**    | `OrDematService`         | Applique les règles métier | Validation et persistance  |
| **Mapper**     | `DemandeOrMapper`        | Convertit DTO ↔ Entité     | `OrDematDTO` → `DemandeOr` |
| **Repository** | `DemandeOrRepository`    | Interagit avec la base     | `save()`                   |
| **Entity**     | `DemandeOr`              | Structure SQL persistée    | Colonne `date_debut`       |
| **Auditing**   | `AbstractAuditingEntity` | Gère la traçabilité        | `createdBy`, `createdDate` |

---

## 7. Points clés à retenir

**Les responsabilités sont bien séparées**
Chaque couche a une fonction unique et claire.

**Les données sont traçables**
Grâce à l’auditing + tests d’intégration.

**La persistance est sécurisée et transactionnelle**
Spring gère le `commit` et le `rollback` automatiquement.

**Liquibase garantit la cohérence du schéma**
Toute évolution est versionnée, reproductible et testable.

---

## 8. Annexe

| Thème                 | Ressource                                                                                                 |
| --------------------- | --------------------------------------------------------------------------------------------------------- |
| Architecture JHipster | [https://www.jhipster.tech/architecture/](https://www.jhipster.tech/architecture/)                        |
| Spring Data JPA       | [https://spring.io/projects/spring-data-jpa](https://spring.io/projects/spring-data-jpa)                  |
| MapStruct             | [https://mapstruct.org/](https://mapstruct.org/)                                                          |
| Liquibase             | [https://www.liquibase.org/](https://www.liquibase.org/)                                                  |
| Auditing JPA          | [Spring Auditing Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#auditing) |

