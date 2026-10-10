package com.lab.sample.config;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.entity.Customer;
import com.lab.sample.entity.TestDefinition;
import com.lab.sample.repository.CustomerRepository;
import com.lab.sample.repository.TestDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sadece "seed" profili aktifken calisir: ./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
 * Klinik biyokimya alaninda ornek musteri ve test kataloji ekler. Native SQL kullanilmaz,
 * sadece repository (JPA) uzerinden yazilir.
 */
@Component
@Profile("seed")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final TestDefinitionRepository testDefinitionRepository;

    @Override
    public void run(String... args) {
        seedCustomers();
        seedTestDefinitions();
    }

    private void seedCustomers() {
        if (customerRepository.count() > 0) {
            return;
        }
        List<Customer> customers = List.of(
                customer("Acıbadem Kadıköy Hastanesi", "lab@acibadem-kadikoy.example", "+90 216 111 22 33"),
                customer("Memorial Şişli Hastanesi", "lab@memorial-sisli.example", "+90 212 222 33 44"),
                customer("Medipol Mega Üniversite Hastanesi", "lab@medipol-mega.example", "+90 212 333 44 55"),
                customer("Florence Nightingale Hastanesi", "lab@florence.example", "+90 212 444 55 66"),
                customer("Aile Sağlığı Merkezi - Beşiktaş", "lab@asm-besiktas.example", "+90 212 555 66 77"));
        customerRepository.saveAll(customers);
    }

    private Customer customer(String name, String email, String phone) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        return customer;
    }

    private void seedTestDefinitions() {
        if (testDefinitionRepository.count() > 0) {
            return;
        }
        List<TestDefinition> definitions = List.of(
                def("GLU", "Glukoz (Açlık)", "mg/dL", "70", "100"),
                def("HGB", "Hemoglobin", "g/dL", "12.0", "16.0"),
                def("HCT", "Hematokrit", "%", "36", "48"),
                def("WBC", "Beyaz Küre Sayımı", "10^3/uL", "4.0", "11.0"),
                def("PLT", "Trombosit Sayımı", "10^3/uL", "150", "400"),
                def("CRE", "Kreatinin", "mg/dL", "0.6", "1.3"),
                def("URE", "Üre", "mg/dL", "10", "50"),
                def("AST", "AST (SGOT)", "U/L", "5", "40"),
                def("ALT", "ALT (SGPT)", "U/L", "7", "56"),
                def("TSH", "Tiroid Stimülan Hormon", "mIU/L", "0.4", "4.0"),
                def("NA", "Sodyum", "mmol/L", "135", "145"),
                def("K", "Potasyum", "mmol/L", "3.5", "5.1"),
                def("CHOL", "Total Kolesterol", "mg/dL", null, "200"),
                def("HDL", "HDL Kolesterol", "mg/dL", "40", null),
                def("LDL", "LDL Kolesterol", "mg/dL", null, "130"),
                def("TRIG", "Trigliserit", "mg/dL", null, "150"),
                def("CRP", "C-Reaktif Protein", "mg/L", null, "5"));
        testDefinitionRepository.saveAll(definitions);
    }

    private TestDefinition def(String code, String name, String unit, String min, String max) {
        TestDefinition definition = new TestDefinition();
        definition.setCode(code);
        definition.setName(name);
        definition.setUnit(unit);
        definition.setRefMin(min == null ? null : new BigDecimal(min));
        definition.setRefMax(max == null ? null : new BigDecimal(max));
        definition.setActive(true);
        return definition;
    }
}
