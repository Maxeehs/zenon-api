package org.alnitaka.zenon.security;

/**
 * Enum représentant les rôles possibles pour un utilisateur.
 */
public enum Role {
	ROLE_USER,
	ROLE_ADMIN
	// /!\ → Il ne faut pas changer l'ordre des rôles, les valeurs en BDD en dépendent
	// Ajoutez ici d’autres rôles si besoin, par ex. ROLE_MANAGER, ROLE_SUPPORT, etc.
}
