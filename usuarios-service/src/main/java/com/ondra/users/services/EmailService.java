package com.ondra.users.services;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de correos electrónicos.
 * Utiliza Spring Mail para el envío de emails transaccionales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        log.info("EmailService inicializado. Email remitente configurado: {}", fromEmail);
        log.info("URL del frontend configurada: {}", frontendUrl);
    }

    /**
     * Envía un correo de verificación de email con un token único.
     *
     * @param destinatario Email del usuario
     * @param nombreUsuario Nombre del usuario
     * @param tokenVerificacion Token único de verificación
     */
    public void enviarEmailVerificacion(String destinatario, String nombreUsuario, String tokenVerificacion) {
        try {
            log.debug("Preparando email de verificación para: {}", destinatario);

            String enlaceVerificacion = frontendUrl + "/api/usuarios/verificar-email?token=" + tokenVerificacion;
            String htmlContent = construirMensajeVerificacionHTML(nombreUsuario, enlaceVerificacion);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromEmail, "OndraSounds");
            helper.setTo(destinatario);
            helper.setSubject("Verifica tu cuenta - OndraSounds");
            helper.setText(htmlContent, true);

            log.debug("Enviando email desde: {} hacia: {}", fromEmail, destinatario);
            mailSender.send(mensaje);
            log.info("Email de verificación enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email de verificación a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Error al enviar el correo de verificación: " + e.getMessage());
        }
    }
    
    /**
     * Construye el mensaje HTML de verificación de email.
     */
    private String construirMensajeVerificacionHTML(String nombreUsuario, String enlaceVerificacion) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Verificación de cuenta</title>
        </head>
        <body style="margin: 0; padding: 0; background-color: #f7f7f7;">
            <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                <tr>
                    <td align="center" style="padding: 20px;">
                        <table role="presentation" cellpadding="0" cellspacing="0" width="600" 
                               style="background-color: #ffffff; padding: 30px; border-radius: 8px; 
                                      box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); font-family: Arial, sans-serif;">
                            
                            <!-- Logo -->
                            <tr>
                                <td align="center" style="padding-bottom: 30px;">
                                    <img src="https://res.cloudinary.com/dh6w4hrx7/image/upload/v1759866494/logo_ondra_lqo3lv.jpg" 
                                         alt="OndraSounds Logo" width="600" 
                                         style="display: block; width: 100%%; max-width: 600px; height: auto;">
                                </td>
                            </tr>

                            <!-- Título -->
                            <tr>
                                <td style="color: black; font-size: 36px; margin-bottom: 20px;">
                                    ¡Hola, %s!
                                </td>
                            </tr>

                            <!-- Contenido -->
                            <tr>
                                <td style="font-size: 16px; line-height: 1.5; color: #555555; padding: 10px 0;">
                                    Miles de usuarios ya están disfrutando de la experiencia OndraSounds.
                                </td>
                            </tr>
                            <tr>
                                <td style="font-size: 16px; line-height: 1.5; color: #555555; padding: 10px 0;">
                                    Desde el equipo de desarrollo GC03, te agradecemos que quieras formar parte de la comunidad, 
                                    estás a un solo paso de hacerlo.
                                </td>
                            </tr>
                            <tr>
                                <td style="font-size: 16px; line-height: 1.5; color: #555555; padding: 10px 0;">
                                    Haz clic en el botón a continuación para confirmar tu correo electrónico:
                                </td>
                            </tr>

                            <!-- Botón -->
                            <tr>
                                <td align="center" style="padding: 30px 0;">
                                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                                        <tr>
                                            <td align="center">
                                                <a href="%s"
                                                   style="display: inline-block;
                                                          padding: 12px 20px;
                                                          background-color: black;
                                                          color: white;
                                                          text-decoration: none;
                                                          border-radius: 5px;
                                                          font-size: 18px;
                                                          text-align: center;">
                                                    Confirmar correo
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>

                            <!-- Nota -->
                            <tr>
                                <td align="center" style="margin-top: 30px; font-size: 12px; color: #777777;">
                                    Si no hiciste esta solicitud, puedes ignorar este mensaje.
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(nombreUsuario, enlaceVerificacion);
    }
}