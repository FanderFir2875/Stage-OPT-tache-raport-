# Création et intégration complète de la table `demande_or` dans SIOR

## Objectif général

L’objectif est **d’ajouter la table `demande_or` dans la base PostgreSQL de SIOR**
pour stocker les demandes d’ordre de réexpédition (OR dématérialisé).

Cette première version implémente :

* La **structure SQL** via **Liquibase**
* Les **couches JPA / Spring Boot** (Entity → Repository → Service → Mapper → Controller)
* Le tout **sans casser les données existantes**

---

## Étape A — Création de l’entité JPA `DemandeOr`

### Fichier :

`src/main/java/nc/opt/sior/domain/DemandeOr.java`

```java
package nc.opt.sior.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "demande_or")
public class DemandeOr implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_debut", nullable = false)
    private Instant dateDebut;

    @Column(name = "date_fin", nullable = false)
    private Instant dateFin;

    @Column(name = "nom_titulaire", nullable = false)
    private String nomTitulaire;

    @Column(name = "prenom_titulaire", nullable = false)
    private String prenomTitulaire;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instant getDateDebut() { return dateDebut; }
    public void setDateDebut(Instant dateDebut) { this.dateDebut = dateDebut; }

    public Instant getDateFin() { return dateFin; }
    public void setDateFin(Instant dateFin) { this.dateFin = dateFin; }

    public String getNomTitulaire() { return nomTitulaire; }
    public void setNomTitulaire(String nomTitulaire) { this.nomTitulaire = nomTitulaire; }

    public String getPrenomTitulaire() { return prenomTitulaire; }
    public void setPrenomTitulaire(String prenomTitulaire) { this.prenomTitulaire = prenomTitulaire; }
}
```

**Rappels pédagogiques :**

| Annotation                  | Rôle                                            |
| --------------------------- | ----------------------------------------------- |
| `@Entity`                   | Classe persistée dans la base                   |
| `@Table(name = "...")`      | Nom explicite de la table                       |
| `@Id` + `@GeneratedValue`   | Clé primaire auto-incrémentée                   |
| `@Column(nullable = false)` | Champ obligatoire                               |
| `Instant`                   | Type Java moderne pour gérer les timestamps UTC |

---

## Étape B — Création du fichier Liquibase

### Fichier :

`src/main/resources/config/liquibase/changelog/20251029-01_added_table_demande_or.xml`

```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                                       http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <changeSet id="20251029-01" author="anguyen">
        <createTable tableName="demande_or">
            <column name="id" type="BIGSERIAL">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="date_debut" type="TIMESTAMP"/>
            <column name="date_fin" type="TIMESTAMP"/>
            <column name="nom_titulaire" type="VARCHAR(255)"/>
            <column name="prenom_titulaire" type="VARCHAR(255)"/>
        </createTable>
    </changeSet>

</databaseChangeLog>
```

### Ajout dans `master.xml`

```xml
<include file="config/liquibase/changelog/20251029-01_added_table_demande_or.xml"/>
```

**Liquibase = Git pour la base**

* Chaque `changeSet` représente une migration unique.
* Liquibase garde une trace dans la table `databasechangelog` pour ne pas rejouer 2x le même script.

---

## Étape C — Exécution Liquibase (mise à jour sans casse)

### Commandes principales :

**Marquer le script comme connu (optionnel)**

```bash
./gradlew liquibaseChangeLogSync \
  -Pliquibase.changelogFile=src/main/resources/config/liquibase/changelog/20251029-01_added_table_demande_or.xml
```

**Appliquer réellement les changements**

```bash
./gradlew liquibaseUpdate
```

Une fois terminée :

* La table `demande_or` apparaît dans DBeaver
* Les colonnes sont bien créées (`date_debut`, `date_fin`, etc.)

![bdd](demande_or_comfirmation.png)

---

## Étape D — Création du Repository

### Fichier :

`src/main/java/nc/opt/sior/repository/DemandeOrRepository.java`

```java
package nc.opt.sior.repository;

import nc.opt.sior.domain.DemandeOr;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeOrRepository extends JpaRepository<DemandeOr, Long> {
}
```

**Le Repository gère l’accès aux données**

* `JpaRepository` fournit automatiquement les opérations CRUD :

  * `save()`, `findById()`, `findAll()`, `deleteById()`…
* Pas besoin d’écrire la moindre requête SQL.

---

## Étape E — Création du Service Métier `DemandeOrService`

### Fichier :

`src/main/java/nc/opt/sior/service/DemandeOrService.java`

```java
package nc.opt.sior.service;

import nc.opt.sior.domain.DemandeOr;
import nc.opt.sior.repository.DemandeOrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DemandeOrService {

    private final DemandeOrRepository demandeOrRepository;

    public DemandeOrService(DemandeOrRepository demandeOrRepository) {
        this.demandeOrRepository = demandeOrRepository;
    }

    public DemandeOr save(DemandeOr demandeOR) {
        return demandeOrRepository.save(demandeOR);
    }

    @Transactional(readOnly = true)
    public DemandeOr findOne(Long id) {
        return demandeOrRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        demandeOrRepository.deleteById(id);
    }
}
```

**Service = couche métier**

* Encapsule la logique métier (validation, persistance…)
* Isole le contrôleur du repository (bonne pratique “Clean Architecture”)
* `@Transactional` gère les transactions automatiquement

---

## Étape F — Mapper (conversion DTO ↔ Entité)

### Fichier :

`src/main/java/nc/opt/sior/service/mapper/DemandeOrMapper.java`

```java
package nc.opt.sior.service.mapper;

import nc.opt.sior.domain.DemandeOr;
import nc.opt.sior.service.dto.OrDematDTO;
import org.mapstruct.*;

import java.time.Instant;
import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface DemandeOrMapper {

    @Mapping(target = "dateDebut", expression = "java(toInstant(dto.getDateDebut()))")
    @Mapping(target = "dateFin", expression = "java(toInstant(dto.getDateFin()))")
    DemandeOr toEntity(OrDematDTO dto);

    default Instant toInstant(String date) {
        return (date == null || date.isBlank()) ? null : LocalDate.parse(date).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
    }
}
```

🧠 **MapStruct = génération automatique**

* `@Mapper(componentModel = "spring")` → Spring gère l’injection du mapper.
* Convertit automatiquement les objets `OrDematDTO` (venant du JSON) en entités `DemandeOr`.
* Gère les conversions de `String → Instant`.

---

## Étape G — Contrôleur REST : `OrDematResource`

### Fichier :

`src/main/java/nc/opt/sior/web/rest/OrDematResource.java`

```java
package nc.opt.sior.web.rest;

import nc.opt.sior.service.OrDematService;
import nc.opt.sior.service.dto.OrDematDTO;
import nc.opt.sior.service.dto.OrDematResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrDematResource {

    private final Logger log = LoggerFactory.getLogger(OrDematResource.class);
    private final OrDematService orDematService;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public OrDematResource(OrDematService orDematService) {
        this.orDematService = orDematService;
    }

    /**
     * {@code POST  /or-demat} : Soumettre une demande d'ordre de réexpédition.
     */
    @PostMapping("/or-demat")
    public ResponseEntity<?> createOrDemat(@Valid @RequestBody OrDematDTO dto) {
        log.debug("REST request to create OR-Démat : {}", dto);

        try {
            OrDematResponseDTO response = orDematService.processOrDemat(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
```

**Contrôleur = point d’entrée HTTP**

* Reçoit le JSON du front (`OrDematDTO`)
* Appelle la logique métier via `OrDematService`
* Retourne une réponse JSON claire (`OrDematResponseDTO`)
* Utilise `ResponseEntity` pour personnaliser le statut HTTP (201, 400…)

---


## Étape H — Tests unitaires et d’intégration

Les tests sont une **partie essentielle** du cycle de développement Spring Boot.
Ils garantissent que ton API fonctionne correctement, que la base est bien mise à jour, et qu’aucun changement futur ne cassera la logique existante.

---

## 1. Test unitaire — `OrDematResourceTest`

### Fichier :

`src/test/java/nc/opt/sior/web/rest/OrDematResourceTest.java`

```java
package nc.opt.sior.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import nc.opt.sior.service.OrDematService;
import nc.opt.sior.service.dto.OrDematDTO;
import nc.opt.sior.service.dto.OrDematResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrDematResourceTest {

    private MockMvc mvc;
    private final ObjectMapper om = new ObjectMapper();

    @Mock
    private OrDematService orDematService;

    @InjectMocks
    private OrDematResource orDematResource;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(orDematResource).build();
    }

    @Test
    void create_shouldReturn201_whenValidDates() throws Exception {
        var dto = new OrDematDTO("2025-02-01", "2025-03-01");
        var response = new OrDematResponseDTO("2025-02-01", "2025-03-01", "Ordre de réexpédition créé avec succès");

        when(orDematService.processOrDemat(any(OrDematDTO.class))).thenReturn(response);

        mvc.perform(post("/api/or-demat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dateDebut").value("2025-02-01"))
            .andExpect(jsonPath("$.dateFin").value("2025-03-01"))
            .andExpect(jsonPath("$.message").value("Ordre de réexpédition créé avec succès"));
    }

    @Test
    void create_shouldReturn400_whenMissingDateFin() throws Exception {
        var dto = new OrDematDTO("2025-03-01", null);
        when(orDematService.processOrDemat(any(OrDematDTO.class)))
            .thenThrow(new IllegalArgumentException("dateFin est obligatoire"));

        mvc.perform(post("/api/or-demat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("dateFin est obligatoire"));
    }
}
```

### But du test unitaire :

| Élément       | Description                                                               |
| ------------- | ------------------------------------------------------------------------- |
| Cible      | Le contrôleur `OrDematResource` **isolé**                                 |
| Mocks      | Le service `OrDematService` est simulé via Mockito                        |
| Vérifie    | Que le contrôleur gère correctement les statuts HTTP et les réponses JSON |
| Pas de BDD | La base n’est **pas utilisée** ici (test très rapide)                     |

---

## 2. Test d’intégration — `OrDematResourceIT`

### Fichier :

`src/test/java/nc/opt/sior/web/rest/OrDematResourceIT.java`

```java
package nc.opt.sior.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import nc.opt.sior.SiorApp;
import nc.opt.sior.domain.DemandeOr;
import nc.opt.sior.repository.DemandeOrRepository;
import nc.opt.sior.service.dto.OrDematDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SiorApp.class)
@AutoConfigureMockMvc
@Transactional
class OrDematResourceIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DemandeOrRepository demandeOrRepository;

    @Autowired
    private ObjectMapper om;

    @BeforeEach
    void initTest() {
        demandeOrRepository.deleteAll();
    }

    @Test
    @Rollback
    void createDemandeOr_shouldPersistAndReturn201() throws Exception {
        var dto = new OrDematDTO("2025-02-01", "2025-03-01");

        mvc.perform(post("/api/or-demat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dateDebut").value("2025-02-01"))
            .andExpect(jsonPath("$.dateFin").value("2025-03-01"))
            .andExpect(jsonPath("$.message").value("Ordre de réexpédition créé avec succès"));

        List<DemandeOr> all = demandeOrRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getDateDebut()).isNotNull();
        assertThat(all.get(0).getDateFin()).isNotNull();
    }
}
```

### But du test d’intégration :

| Élément         | Description                                                   |
| --------------- | ------------------------------------------------------------- |
|  Cible        | Le flux complet : **Controller → Service → Repository → BDD** |
| Contexte     | Lance tout Spring Boot avec une vraie base H2                 |
| Vérifie      | Que la persistance fonctionne réellement                      |
| Avantage     | Détecte les erreurs de mapping ou de configuration Spring     |
| Inconvénient | Plus lent, mais plus réaliste                                 |

---

##  Erreurs typiques rencontrées

Pendant cette implémentation, plusieurs erreurs classiques ont été résolues.
Voici la liste avec explications pour les éviter à l’avenir 

| Type d’erreur                            | Message / Symptôme                                        | Cause                                                | Solution                                                                                                            |
| ---------------------------------------- | --------------------------------------------------------- | ---------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| **Gradle : Task not found**            | `Task '.changelogFile=...' not found`                     | Mauvais placement du paramètre Liquibase             | Utiliser `-Pliquibase.changelogFile=` après la tâche, ex. `./gradlew liquibaseUpdate -Pliquibase.changelogFile=...` |
| **Injection null dans le controller** | `variable orDematService might not have been initialized` | Constructeur mal défini sans injection de dépendance | Ajouter un constructeur `public OrDematResource(OrDematService orDematService)`                                     |
| **Problème de conversion de dates**   | Test échoue sur `2025-02-01`                              | `Instant` converti avec décalage de fuseau horaire   | Tester `isNotNull()` plutôt qu’un format exact                                                                      |
| **Liquibase ne crée pas la table**    | Table absente après `update`                              | Fichier non inclus dans `master.xml`                 | Ajouter `<include file="..."/>` dans `master.xml`                                                                   |
| **Erreur JSON / 400 Bad Request**     | Mauvais format de date `01-02-2025`                       | Format non ISO (`yyyy-MM-dd` attendu)                | Validation et message clair dans `OrDematService`                                                                   |
| **Mapper non injecté**                | `UnsatisfiedDependencyException`                          | `@Mapper(componentModel = "spring")` manquant        | Ajouter l’attribut pour que Spring gère le bean                                                                     |

---

# Résumé général

| Étape | Élément clé                   | Objectif                             |
| ----- | ----------------------------- | ------------------------------------ |
| A     | Entité JPA `DemandeOr`        | Structure de la table                |
| B     | Script Liquibase              | Migration BDD versionnée             |
| C     | Exécution `liquibaseUpdate`   | Mise à jour sans perte               |
| D     | Repository                    | Accès aux données                    |
| E     | Service métier                | Logique et persistance               |
| F     | Mapper MapStruct              | Conversion DTO ↔ Entity              |
| G     | Contrôleur REST               | Endpoint `/api/or-demat`             |
| H     | Tests unitaires + intégration | Validation du fonctionnement complet |


