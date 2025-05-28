
package no.uio.ifi.in2000.simonng.simonng.team1

import no.uio.ifi.in2000.simonng.simonng.team1.data.ml.MLRepository
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

//Gjør her kun "enhetstester" som tester fylkesvalidering og inputhåndtering.

class MLTester {

    @Test
    fun testUgyldigFylke() {
        // Oppretter en MLRepository i test-modus
        val repository = MLRepository(kontekst = null, testModus = true)

        val prediksjon = repository.predikerUlykker("Trondheim", "22.03.2025", 0.0f, 5.1f)

        // Forventer -1 siden "Trondheim" ikke er et gyldig fylke
        assertTrue(-1f == prediksjon)
    }

    @Test
    fun testGyldigFylke() {
        // Oppretter en MLRepository i test-modus
        val repository = MLRepository(kontekst = null, testModus = true)

        val prediksjon = repository.predikerUlykker("Oslo", "22.03.2025", 0.0f, 5.1f)

        // Forventer NotNull ettersom Oslo er et gyldig fylke
        assertNotNull("Prediksjon burde ikke være null for gyldig fylke", prediksjon)
    }
}