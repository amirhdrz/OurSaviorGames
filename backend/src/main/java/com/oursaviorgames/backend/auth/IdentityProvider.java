package com.oursaviorgames.backend.auth;

/**
 * List of OAuth providers.
 * IMPORTANT: IdentityProvider on server should match the identity provider on the client app.
 */
public enum IdentityProvider {
    GOOGLE_PLUS("google.com"),
    FACEBOOK("facebook.com");

    private String domain;

    IdentityProvider(String domain) {
        this.domain = domain;
    }

    /**
     * Returns the domain of this authority.
     * @return
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Returns Enum object matching authorityDomain retrieved from
     * a previous call to {@link IdentityProvider#getDomain()}.
     * @param authorityDomain
     * @return
     */
    public static IdentityProvider parseDomain(String authorityDomain) {
        for (IdentityProvider authority : IdentityProvider.values()) {
            if (authority.domain.equals(authorityDomain)) {
                return authority;
            }
        }
        return null;
    }
}
