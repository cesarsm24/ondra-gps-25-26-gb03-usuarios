package com.ondra.users.models.enums;

/**
 * Enumeración de los tipos de redes sociales soportados.
 *
 * <p>Se permite tener múltiples redes sociales del tipo OTRA,
 * pero solo una de cada uno de los otros tipos por artista.</p>
 */
public enum TipoRedSocial {
    /**
     * Instagram
     */
    INSTAGRAM,

    /**
     * X (anteriormente Twitter)
     */
    X,

    /**
     * Facebook
     */
    FACEBOOK,

    /**
     * YouTube
     */
    YOUTUBE,

    /**
     * TikTok
     */
    TIKTOK,

    /**
     * Spotify
     */
    SPOTIFY,

    /**
     * SoundCloud
     */
    SOUNDCLOUD
}