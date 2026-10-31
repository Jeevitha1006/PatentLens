# PatentLens: AI-Powered Patent Rejection Analyzer

PatentLens is an AI-assisted patent prosecution and claim drafting platform designed for patent attorneys, patent agents, and inventors. It automates the analysis of USPTO/PCT Office Actions, generates plain-language rejection rationales and overcoming claim amendments, performs TF-IDF vector space novelty scoring, and anchors patent drafts to an immutable Proof-of-Invention SHA-256 blockchain.

---

## Technical Stack & Architecture

- **Backend:** Java 17, Spring Boot 3.2.5, Spring Data JPA, Java `HttpClient`
- **Frontend:** Thymeleaf template engine, Bootstrap 5.3 (CDN), Bootstrap Icons 1.11
- **Embedded Database:** H2 file-based persistence (`./data/patentlens.mv.db`)
- **Document Processing:** Apache PDFBox 2.0.30 (PDF text extraction), java-diff-utils 4.12
- **NLP & AI Engine:** Pure Java TF-IDF + Cosine Similarity, Jackson JSON parser, fallback Mock LLM service
- **External APIs:** Semantic Scholar API & OpenAlex API for scholarly prior art

---

## Project Directory Tree

```
c:\Users\JEEVITHA\OneDrive\Desktop\javaproject
├── .mvn/wrapper/
│   └── maven-wrapper.properties
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/patentlens/
    │   │   ├── PatentLensApplication.java
    │   │   ├── controller/
    │   │   │   ├── DashboardController.java
    │   │   │   ├── CompareController.java
    │   │   │   ├── RejectionController.java
    │   │   │   ├── NoveltyController.java
    │   │   │   ├── BlockchainController.java
    │   │   │   ├── PriorArtController.java
    │   │   │   ├── OriginalityController.java
    │   │   │   └── GlobalErrorController.java
    │   │   ├── entity/
    │   │   │   ├── BlockEntity.java
    │   │   │   └── AnalysisHistoryEntity.java
    │   │   ├── model/
    │   │   │   ├── ClaimDiff.java
    │   │   │   ├── ClaimDiffResult.java
    │   │   │   ├── DetectedRejection.java
    │   │   │   ├── RejectionAnalysis.java
    │   │   │   ├── ClaimNoveltyComparison.java
    │   │   │   ├── NoveltyScoreResult.java
    │   │   │   ├── PriorArtArticle.java
    │   │   │   ├── PriorArtSearchResult.java
    │   │   │   └── OriginalityMetricResult.java
    │   │   ├── repository/
    │   │   │   ├── BlockRepository.java
    │   │   │   └── AnalysisHistoryRepository.java
    │   │   ├── service/
    │   │   │   ├── ClaimExtractionService.java
    │   │   │   ├── ClaimDiffService.java
    │   │   │   ├── RejectionDetectorService.java
    │   │   │   ├── LlmService.java
    │   │   │   ├── MockLlmService.java
    │   │   │   ├── ApiLlmService.java
    │   │   │   ├── TfIdfService.java
    │   │   │   ├── NoveltyScoringService.java
    │   │   │   ├── BlockchainService.java
    │   │   │   ├── PriorArtSearchService.java
    │   │   │   └── OriginalityService.java
    │   │   └── util/
    │   │       ├── Sha256Util.java
    │   │       └── DocumentParserUtil.java
    │   └── resources/
    │       ├── application.properties
    │       ├── samples/
    │       │   ├── patent_draft_old.txt
    │       │   ├── patent_draft_new.txt
    │       │   ├── sample_office_action.txt
    │       │   └── sample_prior_art.txt
    │       └── templates/
    │           ├── layout/
    │           │   └── main.html
    │           ├── index.html
    │           ├── compare.html
    │           ├── rejection.html
    │           ├── novelty.html
    │           ├── blockchain.html
    │           ├── prior-art.html
    │           ├── originality.html
    │           └── error.html
    └── test/
        └── java/com/patentlens/
            └── PatentLensApplicationTests.java
```

---

## How to Run PatentLens

### 1. Prerequisites
- **Java 17+** (Oracle JDK, Microsoft JDK, or Eclipse Temurin)
- **Apache Maven 3.8+** (or use the included Maven wrapper)

### 2. Execution Command
Open a terminal (PowerShell, Command Prompt, or Bash) in the project root:

```bash
mvn spring-boot:run
```

Or using the Maven Wrapper:
```powershell
./mvnw spring-boot:run
```

### 3. Accessing the Application
Once the terminal displays `Started PatentLensApplication in X.XXX seconds`:
- Open your browser and navigate to: **`http://localhost:8080`**
- H2 Web Console: **`http://localhost:8080/h2-console`**
  - JDBC URL: `jdbc:h2:file:./data/patentlens`
  - User: `sa`
  - Password: *(leave blank)*

---

## 5-Minute Demo Script (Viva / Presentation Flow)

Follow this structured demonstration flow to showcase all 7 features smoothly in 5 minutes:

### Minute 1: Dashboard & System Overview
1. Navigate to `http://localhost:8080`.
2. Highlight the clean executive dashboard:
   - **Blockchain Height** showing active immutable block anchors.
   - **Analyses Completed** counter updating in real-time.
   - Mention that PatentLens runs **100% locally with zero required external dependencies or paid APIs**.
   - Show the legal disclaimer in the footer: *"Drafting aid only, not legal advice."*

### Minute 2: Feature 1 — Upload & Compare
1. Click **Upload & Compare** in the top navigation bar.
2. Click the top-right button: **"Load Pre-Examination vs Amended Sample"**.
3. Point out the summary badges:
   - 5 Original claims compared against 5 Amended claims.
   - Modified claims highlighted in amber, added clauses in green, and removed clauses in red strikethrough.
4. Show **Claim 1**: Point out how broad GPS navigation was narrowed down to real-time Kalman sensor fusion combining LiDAR and stereo imaging.

### Minute 3: Feature 2 — Rejection Explainer
1. Click **Rejection Explainer** in the navigation bar.
2. Click **"Load USPTO Office Action Sample"**.
3. Scroll through the detected statutory grounds:
   - **35 U.S.C. § 101:** Explains the Alice Step 2B abstract idea rejection and provides an amended hardware-bound claim with continuous 100 Hz sensor sampling.
   - **35 U.S.C. § 102:** Identifies anticipation by *Smith et al.* (US 2019/0084567) and suggests specific structural limitations to overcome anticipation.
   - **35 U.S.C. § 103:** Identifies the obviousness combination of *Smith et al.* and *Johnson* (US 2021/0043210), proposing octree voxel grid mapping.
   - **35 U.S.C. § 112:** Detects lack of antecedent basis for *"said ground landing obstacle"* and demonstrates the exact drafting fix.
4. Note the badge: **"Deterministic Prosecution Engine (Offline Mode)"** ensures the demo never breaks even if offline.

### Minute 4: Feature 3 — TF-IDF Novelty Scoring Heatmap
1. Click **Novelty Scoring** in the navigation bar.
2. Click **"Load Drone Patent vs Smith Prior Art"**.
3. Examine the **Claim Novelty Heatmap Table**:
   - **Amended Novelty Score:** ~78.4% (Green / High Novelty).
   - **Original Novelty Score:** ~39.1% (Red / High Prior-Art Overlap).
   - **Prosecution Delta:** **+39.3%**, mathematically proving that the claim amendment successfully reduced conceptual overlap with the examiner's cited prior art.
4. Reference the **Top Distinctive Prior-Art Terms** extracted via TF-IDF.

### Minute 5: Features 4, 5 & 6 — Blockchain, Prior-Art & Stylometry
1. **Blockchain Proof-of-Invention:**
   - Click **Blockchain Proof**.
   - Show Block #0 (Genesis Block) with initial SHA-256 root and Proof-of-Work.
   - Click **"Verify Blockchain Integrity"**: Green banner verifies unbroken cryptographic linkages.
   - Click **"Test Sample Verification"**: Instant lookup confirms document authenticity and exact block timestamp.
2. **Prior-Art Search:**
   - Click **Prior-Art Search**, click **"Query with Drone Patent Claims"**.
   - Show how the NLP engine extracts top terms (`sensor`, `unmanned`, `fusion`, `controller`) to search Semantic Scholar & OpenAlex.
3. **Originality Indicator:**
   - Click **Originality Indicator**, click **"Analyze Drone Patent Specification"**.
   - Review the **AI-Likeness Risk Gauge** (28/100, Low Risk / Natural Human-like Burstiness), sentence length variance (&sigma;&sup2; = 84.5), and Type-Token Ratio (0.582).

---

## Mathematical Foundations & Viva Defense Q&A

### 1. TF-IDF and Cosine Similarity
- **Term Frequency:** $\text{TF}(t, d) = \frac{f_{t, d}}{\sum_{t' \in d} f_{t', d}}$
  - Normalizes word frequency by total words in the claim to eliminate bias toward verbose claims.
- **Inverse Document Frequency:** $\text{IDF}(t, D) = \ln\left(1 + \frac{|D|}{1 + \text{DF}(t, D)}\right) + 1.0$
  - Smoothed IDF that down-weights generic boilerplate ("comprising", "apparatus") and boosts distinct technical terms ("pneumatic", "voxel").
- **Cosine Similarity:** $\cos(\mathbf{u}, \mathbf{v}) = \frac{\mathbf{u} \cdot \mathbf{v}}{\|\mathbf{u}\|_2 \|\mathbf{v}\|_2}$
  - Geometric dot product of unit vectors representing directional semantic alignment between 0.0 (orthogonal/novel) and 1.0 (identical overlap).

### 2. Proof-of-Invention Blockchain
- **Block Mining:** Nonce is incremented sequentially until $\text{SHA-256}(\text{Index} + \text{Timestamp} + \text{PrevHash} + \text{DocHash} + \text{Nonce})$ starts with target leading zeros (`00`).
- **Tamper Detection:** Auditing recomputes the SHA-256 hash of every block from 0 to N. If any byte of an anchored document is altered, its block hash changes, breaking the `previousHash` pointer of all subsequent blocks.

### 3. Stylometry Heuristics (AI-Likeness Risk)
- **Burstiness (Sentence Length Variance):** $\sigma^2 = \frac{1}{N}\sum_{i=1}^N (L_i - \mu)^2$. Human writing features high variance, mixing short assertions with compound clauses. LLMs produce uniform, rhythmic sentence lengths (low $\sigma^2$).
- **Type-Token Ratio (TTR):** $\text{TTR} = \frac{|V|}{N}$, measuring unique vocabulary richness against overall text length.

---

## Disclaimer
PatentLens is an educational and drafting aid only, not legal advice. Patent applications, office actions, and claim amendments must be reviewed by a licensed patent attorney or agent before submission to the USPTO or international patent offices.
