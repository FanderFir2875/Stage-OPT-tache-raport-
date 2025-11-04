# Tâche : Implémentation de l’endpoint POST pour la création d’un OR définitif

## Objectif

Mise en place d’un **endpoint REST POST `/api/or-definitif`** permettant la création d’un **Ordre de Réexpédition (OR) définitif**.
Les données reçues (payload JSON) sont stockées **intégralement dans la table `demande_or`** via une colonne `data` de type **JSONB** (PostgreSQL) ou **CLOB** (H2 pour les tests).

---

## Détails d’implémentation

* **Nouvelle colonne `data`** ajoutée dans `demande_or` via Liquibase :

  * Type : `jsonb` pour PostgreSQL, `clob` pour H2
  * Contrainte : `nullable = false`

  ```xml
  <changeSet id="20251103-01" author="a.nguyen">
      <addColumn tableName="demande_or" dbms="postgresql">
          <column name="data" type="jsonb">
              <constraints nullable="false"/>
          </column>
      </addColumn>

      <addColumn tableName="demande_or" dbms="h2">
          <column name="data" type="clob">
              <constraints nullable="false"/>
          </column>
      </addColumn>
  </changeSet>
  ```

---

### Entité `DemandeOr.java`

Mise à jour de la structure pour inclure le champ JSON complet de la demande OR :

```java
@Entity
@Table(name = "demande_or")
@Getter
@Setter
public class DemandeOr extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "date_debut", nullable = false)
    private Instant dateDebut;

    @Column(name = "date_fin", nullable = false)
    private Instant dateFin;

    @Lob
    @Column(name = "data", nullable = false)
    private String data;
}
```

---

### Service métier `OrDematService.java`

Ajout de la méthode de création d’un OR définitif.
Le payload JSON est reçu sous forme de `Map`, puis sérialisé et sauvegardé dans la colonne `data`.

```java
@Service
@Transactional
@Slf4j
public class OrDematService {

    private final DemandeOrRepository demandeOrRepository;
    private final ObjectMapper objectMapper;

    public OrDematService(DemandeOrRepository demandeOrRepository, ObjectMapper objectMapper) {
        this.demandeOrRepository = demandeOrRepository;
        this.objectMapper = objectMapper;
    }

    public DemandeOr createDemandeOr(Map<String, Object> payload) {
        log.info("📩 Création d’une demande OR définitif : {}", payload);

        try {
            DemandeOr demandeOr = new DemandeOr();
            demandeOr.setData(objectMapper.writeValueAsString(payload));
            return demandeOrRepository.save(demandeOr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur de conversion JSON", e);
        }
    }
}
```

---

### Contrôleur `OrDematResource.java`

Ajout d’un **endpoint POST `/api/or-definitif`** pour recevoir et sauvegarder une demande OR définitif.

```java
@RestController
@RequestMapping("/api")
@Slf4j
public class OrDematResource {

    private final OrDematService orDematService;

    public OrDematResource(OrDematService orDematService) {
        this.orDematService = orDematService;
    }

    @PostMapping("/or-definitif")
    public ResponseEntity<DemandeOr> createDemandeOR(@RequestBody Map<String, Object> payload) {
        log.info("➡️ Requête reçue pour création d’un OR définitif : {}", payload);

        DemandeOr result = orDematService.createDemandeOr(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
```

---

## Résultats attendus

* Endpoint **POST `/api/or-definitif`** créé et fonctionnel
* Les données JSON sont **persistées dans la table `demande_or`**
* Le champ `data` est bien typé **JSONB** en base PostgreSQL
* Le **Liquibase update** s’exécute sans erreur
* Le **JSON complet** est visible dans la colonne `data` après insertion

---

## Exemple d’appel API

**POST** `/api/or-definitif`

**Requête :**

```json
{
  "typeOr": "DEFINITIF",
  "dateDebut": "2025-11-05T00:00:00Z",
  "dateFin": "2025-12-05T00:00:00Z",
  "adresseOrigine": "X",
  "adresseDestination": "X"
}
```

**Réponse (201 Created) :**

```json
{
  "id": 42,
  "data": "{\"typeOr\":\"DEFINITIF\",\"adresseDestination\":\"4 avenue du Port, Dumbéa\", ...}"
}
```

---

## Structure Spring mise en place

| Couche         | Fichier principal          | Rôle                                      |
| -------------- | -------------------------- | ----------------------------------------- |
| **Controller** | `OrDematResource.java`     | Exposition REST — réception du JSON       |
| **Service**    | `OrDematService.java`      | Logique métier et sérialisation JSON      |
| **Repository** | `DemandeOrRepository.java` | Accès JPA à la table `demande_or`         |
| **Entity**     | `DemandeOr.java`           | Structure de persistance avec champ JSONB |

---

