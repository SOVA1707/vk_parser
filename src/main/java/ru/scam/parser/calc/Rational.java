package ru.scam.parser.calc;

public class Rational {
    private int numerator;// числитель
    private int denominator; // знаменатель

    /**
     * Констуктор с параметрами с явной проверкой на ноль
     *
     * @param numerator Числитель
     * @param denominator - Знаменатель
     */
    public Rational(int numerator, int denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("Знаменатель не может быть равен нулю");
        }
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Вывод дроби
     *
     * @return String numerator/denominator
     */
    @Override
    public String toString() {
        if (denominator == 1) return String.valueOf(numerator);
        return numerator + "/" + denominator;
    }

    /**
     * Метод, для подсчета умножения двух дробей
     *
     * @param fraction - Объект дроби в качестве множителя
     */
    public void Multiplication(Rational fraction) {
        this.numerator = this.numerator * fraction.numerator;
        this.denominator = this.denominator * fraction.denominator;
    }

    /**
     * Метод, для подсчета деления двух дробей
     *
     * @param fraction - Объект дроби в качестве делителя
     */
    public void Division(Rational fraction) {
        if (fraction.numerator == 0){
            throw new ArithmeticException("Числитель делителя равен 0..");
        }

        this.numerator = this.numerator * fraction.denominator;
        this.denominator = this.denominator * fraction.numerator;
    }

    /**
     * Метод, для подсчета сложения двух дробей
     *
     * @param fraction - Объект дроби в качестве слагаемого
     */
    public void Addition(Rational fraction) {
        this.numerator = (this.numerator * fraction.denominator) + (this.denominator * fraction.numerator);
        this.denominator = this.denominator * fraction.denominator;
    }

    /**
     * Метод, для подсчета вычитания двух дробей
     *
     * @param fraction - Объект дроби в качестве вычитаемого
     */
    public void Subtraction(Rational fraction) {
        this.numerator = (this.numerator * fraction.denominator) - (this.denominator * fraction.numerator);
        this.denominator = this.denominator * fraction.denominator;
    }

    /**
     * Метод, для сокращения дроби к меньшим значениям
     */
    public void Reduction(){
        int n = this.numerator;
        int d = this.denominator;

        for(int i = this.numerator; i > 1; i--){
            if((this.numerator % i == 0) && (this.denominator % i == 0)){
                n = this.numerator / i;
                d = this.denominator / i;
            }
        }

        this.numerator = n;
        this.denominator = d;
    }
}