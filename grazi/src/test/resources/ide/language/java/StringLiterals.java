class Main {
    public static void main(String[] args) {
        String oneTypo = "It is <warning>friend</warning> of human";
        String oneSpellcheckTypo = "It is <warning>frend</warning> of human";
        String fewTypos = "It <warning>are</warning> working for <warning>much</warning> warnings";
        String ignoreTemplate = "It is ${1} friend";
        String notIgnoreOtherMistakes = "It is <warning>friend</warning>. <warning>But</warning> I have a ${1} here";

        System.out.println("It is <warning>friend</warning> of human");
        System.out.println("It is <warning>frend</warning> of human");
        System.out.println("It <warning>are</warning> working for <warning>much</warning> warnings");
        System.out.println("It is ${1} friend");
        System.out.println("It is <warning>friend</warning>. <warning>But</warning> I have a ${1} here");
    }
}
