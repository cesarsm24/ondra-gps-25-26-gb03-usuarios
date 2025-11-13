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

            String enlaceVerificacion = frontendUrl + "/login?token=" + tokenVerificacion;
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
     * Envía un correo de recuperación de contraseña con código de 6 dígitos.
     * Solo envía el código, sin enlace con token.
     *
     * @param destinatario Email del usuario
     * @param nombreUsuario Nombre del usuario
     * @param codigoVerificacion Código de 6 dígitos
     */
    public void enviarEmailRecuperacion(String destinatario, String nombreUsuario, String codigoVerificacion) {
        try {
            log.debug("Preparando email de recuperación con código para: {}", destinatario);

            String htmlContent = construirMensajeRecuperacionConCodigo(nombreUsuario, codigoVerificacion);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromEmail, "OndraSounds");
            helper.setTo(destinatario);
            helper.setSubject("Recuperación de contraseña - OndraSounds");
            helper.setText(htmlContent, true);

            mailSender.send(mensaje);
            log.info("Email de recuperación con código enviado a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Error al enviar el correo de recuperación: " + e.getMessage());
        }
    }

    /**
     * Envía un correo de confirmación después de cambiar la contraseña.
     *
     * @param destinatario Email del usuario
     * @param nombreUsuario Nombre del usuario
     */
    public void enviarEmailConfirmacionCambioPassword(String destinatario, String nombreUsuario) {
        try {
            log.debug("Preparando email de confirmación de cambio de contraseña para: {}", destinatario);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromEmail, "OndraSounds");
            helper.setTo(destinatario);
            helper.setSubject("Tu contraseña ha sido cambiada - OndraSounds");
            helper.setText(construirMensajeConfirmacionCambio(nombreUsuario), true);

            mailSender.send(mensaje);
            log.info("Email de confirmación de cambio de contraseña enviado a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación a {}: {}", destinatario, e.getMessage(), e);
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

    /**
     * Construye el mensaje HTML de recuperación con código de 6 dígitos.
     */
    private String construirMensajeRecuperacionConCodigo(String nombreUsuario, String codigoVerificacion) {
        // Convertir el código en dígitos individuales
        StringBuilder digitosHTML = new StringBuilder();
        for (char digito : codigoVerificacion.toCharArray()) {
            digitosHTML.append(String.format("""
            <td style="width: 50px; height: 60px; background-color: #f0f0f0;
                       text-align: center; vertical-align: middle;
                       font-size: 32px; font-weight: bold; font-family: monospace;
                       border: 2px solid #dddddd; border-radius: 8px;">%c</td>
            """, digito));
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Recuperación de contraseña</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #f5f5f5; font-family: Arial, sans-serif;">
                <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 40px 20px;">
                            <table role="presentation" cellpadding="0" cellspacing="0" width="600" 
                                   style="background-color: #ffffff; padding: 40px; border-radius: 12px; 
                                          box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
        
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
                                    <td align="center" style="padding-bottom: 10px;">
                                        <h1 style="margin: 0; font-size: 28px; font-weight: bold; color: #333333;">
                                            ¡Hola, %s!
                                        </h1>
                                    </td>
                                </tr>
        
                                <!-- Subtítulo -->
                                <tr>
                                    <td align="center" style="padding-bottom: 30px;">
                                        <p style="margin: 0; font-size: 16px; color: #666666; line-height: 1.5;">
                                            Recibimos una solicitud para restablecer la contraseña de tu cuenta.
                                        </p>
                                    </td>
                                </tr>
        
                                <!-- Instrucción -->
                                <tr>
                                    <td align="center" style="padding-bottom: 20px;">
                                        <p style="margin: 0; font-size: 16px; color: #333333; font-weight: 600;">
                                            Introduce este código en la aplicación:
                                        </p>
                                    </td>
                                </tr>
        
                                <!-- Código de 6 dígitos -->
                                <tr>
                                    <td align="center" style="padding-bottom: 30px;">
                                        <table role="presentation" border="0" cellpadding="0" cellspacing="8" align="center">
                                            <tr>
                                                %s
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
        
                                <!-- Información de expiración -->
                                <tr>
                                    <td align="center" style="padding-bottom: 20px;">
                                        <div style="background-color: #fff3cd; border: 1px solid #ffc107; 
                                                    border-radius: 8px; padding: 15px; margin: 0 20px;">
                                            <p style="margin: 0; font-size: 14px; color: #856404;">
                                                ⏰ <strong>Este código expira en 1 hora</strong>
                                            </p>
                                        </div>
                                    </td>
                                </tr>
        
                                <!-- Separador -->
                                <tr>
                                    <td style="padding: 20px 0;">
                                        <div style="border-top: 1px solid #eeeeee;"></div>
                                    </td>
                                </tr>
        
                                <!-- Nota de seguridad -->
                                <tr>
                                    <td style="padding: 0 20px;">
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #666666; line-height: 1.5;">
                                            <strong>¿No solicitaste este cambio?</strong>
                                        </p>
                                        <p style="margin: 0; font-size: 14px; color: #666666; line-height: 1.5;">
                                            Si no fuiste tú quien solicitó restablecer la contraseña, 
                                            ignora este correo y tu cuenta permanecerá segura. 
                                            Nadie podrá cambiar tu contraseña sin este código.
                                        </p>
                                    </td>
                                </tr>
        
                                <!-- Footer -->
                                <tr>
                                    <td align="center" style="padding-top: 30px;">
                                        <p style="margin: 0; font-size: 12px; color: #999999;">
                                            Este es un correo automático, por favor no respondas a este mensaje.
                                        </p>
                                        <p style="margin: 10px 0 0 0; font-size: 12px; color: #999999;">
                                            © 2025 OndraSounds - Desarrollado por GC03
                                        </p>
                                    </td>
                                </tr>
        
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(nombreUsuario, digitosHTML.toString());
    }

    /**
     * Construye el mensaje de confirmación de cambio de contraseña.
     */
    private String construirMensajeConfirmacionCambio(String nombreUsuario) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Contraseña cambiada</title>
        </head>
        <body style="margin: 0; padding: 0; background-color: #f5f5f5;">
            <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                <tr>
                    <td align="center" style="padding: 40px 0;">
                        <table role="presentation" cellpadding="0" cellspacing="0" width="600" 
                               style="background-color: #ffffff; padding: 40px; border-radius: 8px; font-family: sans-serif;">
                            
                            <!-- Logo -->
                            <tr>
                                <td align="center" style="padding-bottom: 30px;">
                                    <img src="https://res.cloudinary.com/dh6w4hrx7/image/upload/v1759866494/logo_ondra_lqo3lv.jpg" 
                                         alt="OndraSounds Logo" width="600"
                                         style="display: block; width: 100%%; max-width: 600px; height: auto;">
                                </td>
                            </tr>
                            
                            <tr>
                                <td style="font-size: 24px; font-weight: bold; color: #333333; padding-bottom: 20px;">
                                    ¡Hola, %s!
                                </td>
                            </tr>
                            
                            <tr>
                                <td style="font-size: 16px; line-height: 1.5; color: #555555; padding: 10px 0;">
                                    Te confirmamos que tu contraseña de OndraSounds ha sido cambiada exitosamente.
                                </td>
                            </tr>
                            
                            <tr>
                                <td style="font-size: 16px; line-height: 1.5; color: #555555; padding: 10px 0;">
                                    Si NO realizaste este cambio, por favor contacta con nuestro equipo de soporte inmediatamente:
                                    <strong>soporte@ondrasounds.com</strong>
                                </td>
                            </tr>
                            
                            <tr>
                                <td style="font-size: 16px; line-height: 1.5; color: #555555; padding: 10px 0;">
                                    Por tu seguridad, todas las sesiones activas en otros dispositivos han sido cerradas.
                                </td>
                            </tr>
                            
                            <tr>
                                <td style="font-size: 14px; color: #777777; padding-top: 30px;">
                                    Saludos,<br>
                                    El equipo de OndraSounds
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(nombreUsuario);
    }
}