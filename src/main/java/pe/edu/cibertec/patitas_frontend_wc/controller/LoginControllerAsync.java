package pe.edu.cibertec.patitas_frontend_wc.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc.client.AutenticacionClient;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    //Llamado al autenticacionClient
    @Autowired
    AutenticacionClient autenticacionClient;

    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO) {

        // validar campos de entrada
        if (loginRequestDTO.tipoDocumento() == null || loginRequestDTO.tipoDocumento().trim().length() == 0 ||
                loginRequestDTO.numeroDocumento() == null || loginRequestDTO.numeroDocumento().trim().length() == 0 ||
                loginRequestDTO.password() == null || loginRequestDTO.password().trim().length() == 0){
            return Mono.just(new LoginResponseDTO("01", "Error: Debe completar correctamente sus credenciales", "", ""));
        }

        try {

            // consumir servicio backend de autenticacion
            return webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response -> {

                        if(response.codigo().equals("00")){
                            return Mono.just(new LoginResponseDTO("00", "", response.nombreUsuario(), ""));
                        } else {
                            return Mono.just(new LoginResponseDTO("02", "Error: Autenticación fallida", "", ""));
                        }

                    });

        } catch(Exception e) {

            System.out.println(e.getMessage());
            return Mono.just(new LoginResponseDTO("99", "Error: Ocurrió un problema en la autenticación", "", ""));

        }

    }

    @PostMapping("/logout-async")
    public Mono<LogoutResponseDTO> logout(@RequestBody LogoutRequestDTO logoutRequestDTO) {
        System.out.println("Consumiendo con Web Client!!!!");
        try {
            return webClientAutenticacion.post()
                    .uri("/logout")
                    .body(Mono.just(logoutRequestDTO), LogoutResponseDTO.class)
                    .retrieve()
                    .bodyToMono(LogoutResponseDTO.class)
                    .flatMap(response ->{
                        if(response.resultado().equals(true)){
                            System.out.println("Cerrado de sesión exitoso");
                            return Mono.just(new LogoutResponseDTO(true,response.fecha(),response.mensajeError()));
                        }else {
                            System.out.println("Error: No se pudo cerrar sesión");
                            return Mono.just(new LogoutResponseDTO(false,null,"Error: No se pudo cerrar sesion"));
                        }
                    });
        } catch (Exception e) {
            System.out.println("Error en logout: " + e.getMessage());
            return Mono.just(new LogoutResponseDTO(false,null,"Error: Error en el logout"));
        }
    }

    //Implementando la autenticacionClient al Logout con algunas impresiones en la consola
    @PostMapping("/feignclient-logout")
    public LogoutResponseDTO logoutFeign(@RequestBody LogoutRequestDTO logoutRequestDTO) {
        System.out.println("Consumiendo con Feign Client!!!!");

        try {
            ResponseEntity<LogoutResponseDTO> responseEntity = autenticacionClient.logout(logoutRequestDTO);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                LogoutResponseDTO response = responseEntity.getBody();
                System.out.println("Response del logout recibido: " + response);

                if (response != null && response.resultado()) {
                    System.out.println("Cerrado de sesión exitoso");
                    return new LogoutResponseDTO(true, response.fecha(), null);
                } else {
                    System.out.println("Error: No se pudo cerrar sesión");
                    return new LogoutResponseDTO(false, null, "Error: No se pudo cerrar sesión");
                }
            } else {
                return new LogoutResponseDTO(false, null, "Error: Problema al invocar el servicio de logout");
            }
        } catch (Exception e) {
            System.out.println("Error en logout: " + e.getMessage());
            return new LogoutResponseDTO(false, null, "Error: Error en el proceso de logout");
        }
    }

}