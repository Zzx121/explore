package cn.edu.djtu.db.entity;

public enum Gender {
    MALE(0), FEMALE(1), SECRET(2);
    private int value;
    private Gender(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
    public static Gender getGenderByValue(Integer value) {
        switch (value) {
            case 0:
                return MALE;
            case 1:
                return FEMALE;
            case 2:
                return SECRET;
            default:
                return SECRET;
        }
    }
}
