package com.loanapi;

import com.loanapi.model.*;
import com.loanapi.repository.LoanInstallmentRepository;
import com.loanapi.repository.LoanRepository;
import com.loanapi.repository.RoleRepository;
import com.loanapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("#{'${loan.api.create.test.data}'}")
    private boolean createTestData;

    public DataLoader(UserRepository userRepository, RoleRepository roleRepository, LoanRepository loanRepository,
                      LoanInstallmentRepository loanInstallmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.loanRepository = loanRepository;
        this.loanInstallmentRepository = loanInstallmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role(ERole.ROLE_ADMIN);
            Role customerRole = new Role((ERole.ROLE_CUSTOMER));

            roleRepository.save(adminRole);
            roleRepository.save(customerRole);
        }

        // Eğer hiç kullanıcı yoksa, ilk kullanıcıyı admin olarak ekle
        if (userRepository.count() == 0) {

            // İlk kullanıcıyı admin olarak ekleyelim
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@loanapi.com");
            adminUser.setPassword(passwordEncoder.encode("admin")); // Şifreyi encode et
            adminUser.setCreditLimit(new BigDecimal(100000));
            adminUser.setUsedCreditLimit(new BigDecimal(0));
            adminUser.setName("Mr.");
            adminUser.setSurname("Admin");
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElse(new Role(ERole.ROLE_ADMIN));
            if(adminUser.getRoles() != null) {
                adminUser.getRoles().add(adminRole);
            } else {
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
            }
            userRepository.save(adminUser);

            System.out.println("Admin kullanıcısı başarıyla oluşturuldu!");

            //To Create Test data below set loan.api.create.test.data = true in application.properties
            if (createTestData) {
                // İkinci kullanıcıyı customer olarak ekleyelim
                User customerUser = new User();
                customerUser.setUsername("customer");
                customerUser.setEmail("customer@loanapi.com");
                customerUser.setPassword(passwordEncoder.encode("customer")); // Şifreyi encode et
                customerUser.setCreditLimit(new BigDecimal(50000));
                customerUser.setUsedCreditLimit(new BigDecimal(30000));
                customerUser.setName("Mr.");
                customerUser.setSurname("Customer");
                Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER).orElse(new Role(ERole.ROLE_CUSTOMER));
                if (customerUser.getRoles() != null) {
                    customerUser.getRoles().add(customerRole);
                } else {
                    Set<Role> roles = new HashSet<>();
                    roles.add(customerRole);
                }
                userRepository.save(customerUser);

                System.out.println("Customer kullanıcısı başarıyla oluşturuldu!");

                //Loan ve Loan Installment oluşturalım
                Loan customerLoan = new Loan();
                customerLoan.setLoanAmount(new BigDecimal(30000));
                customerLoan.setUser(customerUser);
                customerLoan.setPaid(false);
                customerLoan.setNumberOfInstallment(6);
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -2);
                calendar.add(Calendar.DAY_OF_MONTH, 3);
                customerLoan.setCreateDate(format.parse("2024/08/20 11:59:59"));
                customerLoan = loanRepository.save(customerLoan);

                System.out.println("Örnek kredi başarıyla oluşturuldu!");

                LoanInstallment installment1 = new LoanInstallment(
                        customerLoan,
                        new BigDecimal(5000),
                        new BigDecimal(5000),
                        format.parse("2024/09/20 23:59:59"),
                        format.parse("2024/09/20 13:59:59"),
                        true);
                loanInstallmentRepository.save(installment1);

                LoanInstallment installment2 = new LoanInstallment(
                        customerLoan,
                        new BigDecimal(5000),
                        new BigDecimal(5000),
                        format.parse("2024/10/20 23:59:59"),
                        format.parse("2024/10/20 13:59:59"),
                        true);
                loanInstallmentRepository.save(installment2);

                LoanInstallment installment3 = new LoanInstallment(
                        customerLoan,
                        new BigDecimal(5000),
                        new BigDecimal(0),
                        format.parse("2024/11/20 23:59:59"),
                        null,
                        false);
                loanInstallmentRepository.save(installment3);

                LoanInstallment installment4 = new LoanInstallment(
                        customerLoan,
                        new BigDecimal(5000),
                        new BigDecimal(0),
                        format.parse("2024/12/20 23:59:59"),
                        null,
                        false);
                loanInstallmentRepository.save(installment4);

                LoanInstallment installment5 = new LoanInstallment(
                        customerLoan,
                        new BigDecimal(5000),
                        new BigDecimal(0),
                        format.parse("2025/01/20 23:59:59"),
                        null,
                        false);
                loanInstallmentRepository.save(installment5);

                LoanInstallment installment6 = new LoanInstallment(
                        customerLoan,
                        new BigDecimal(5000),
                        new BigDecimal(0),
                        format.parse("2025/02/20 23:59:59"),
                        null,
                        false);
                loanInstallmentRepository.save(installment6);

                System.out.println("Örnek kredi taksitleri başarıyla oluşturuldu!");
            }
        }
    }
}
