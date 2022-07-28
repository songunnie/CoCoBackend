package com.igocst.coco.domain;

public enum MemberRole {
    MEMBER(Authority.MEMBER),
    ADMIN(Authority.ADMIN);

    private final String authority;

    MemberRole(String authority) {this.authority = authority;}

    public String getAuthority() {
        return this.authority;
    }

    public static class Authority {
        public static final String MEMBER = "ROLE_MEMBER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}
