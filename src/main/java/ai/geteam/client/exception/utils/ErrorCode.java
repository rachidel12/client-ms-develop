package ai.geteam.client.exception.utils;

public enum ErrorCode {

    // Message d’erreur général pour les 500
    GENERAL_EXCEPTION("general_error_001"),

    // Utilisateur n’existe pas
    USER_NOT_FOUND("clientgeneral_001"),

    // Si l’utilisateur et l’admin n’existe pas dans la même équipe
    USER_NOT_IN_SAME_TEAM("clientgeneral_002"),

    // l'utilisateur qui appelle cette api n'est pas admin
    USER_NOT_ADMIN("clientgeneral_003"),

    // Champ admin vide
    ADMIN_EMPTY("clientgeneral_004"),

    // Champ admin invalide
    ADMIN_INVALID("clientgeneral_005"),

    // l'email appartient à un utilisateur déja créée sur keycloak
    EMAIL_ALREADY_EXISTS("clientgeneral_006"),

    // Le champ email est vide
    EMAIL_EMPTY("clientgeneral_007"),

    // l'email est invalide
    EMAIL_INVALID("clientgeneral_008"),

    // Le champ name est vide
    NAME_EMPTY("clientgeneral_009"),

    // Le champ signature est vide
    SIGNATURE_EMPTY("clientgeneral_010"),

    // La valeur de name est invalide (enum)
    NAME_INVALID("clientgeneral_011"),

    // Le format de l’image de la signature est invalide
    SIGNATURE_FORMAT_INVALID("clientgeneral_012"),

    // La taille de l’image de la signature dépasse 1 MB
    SIGNATURE_SIZE_INVALID("clientgeneral_013"),

    // La signature avec signatureId n’existe pas.
    SIGNATURE_NOT_FOUND("clientgeneral_015"),

    // Cette signature ne peut pas être supprimée car elle est par défaut.
    SIGNATURE_NOT_DELETED("clientgeneral_016"),

    // Client Invalide
    CLIENT_INVALID("clientgeneral_017"),

    // Utilisateur dèja activé
    USER_ALREADY_ACTIVATED("clientgeneral_018"),

    // Utilisateur dèja bloqué
    USER_ALREADY_BLOCKED("clientgeneral_019"),

    // Valeur de firstname est invalide
    FIRSTNAME_INVALID("clientgeneral_020"),

    // Valeur de lastname est invalide
    LASTNAME_INVALID("clientgeneral_021"),

    // Utilisateur bloqué
    USER_IS_BLOCKED("clientgeneral_024"),

    // Access Token vide
    ACCESS_TOKEN_EMPTY("clientauth_001"),

    // Access Token expiré
    ACCESS_TOKEN_EXPIRED("clientauth_002"),

    // Access token n’appartient pas au realm i.e. CLIENT
    ACCESS_TOKEN_NOT_REALM_CLIENT("clientauth_003"),

    // Access token n’a pas le rôle CLIENT
    ACCESS_TOKEN_NOT_ROLE_CLIENT("clientauth_004"),

    NO_USER_WITH_THIS_EMAIL("client_general_023"),
    PHONE_INVALID("client_general_022"),

    // Access Token invalide

    ACCESS_TOKEN_INVALID("clientauth_005"),
    FIRST_NAME_INVALID("client_general_020"),
    LAST_NAME_INVALID("client_general_021");

    private final String error;

    ErrorCode(String error) {
        this.error = error;
    }

    public String get() {
        return this.error;
    }

}
