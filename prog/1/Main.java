public class Main {
    public static void main(String[] args) {
        long[] k = new long[17 - 3 + 1];
        for (int i = 0; i < k.length; i++) {
            k[i] = k.length - i - 1 + 3;
        }

        double[] x = new double[15];
        for (int i = 0; i < x.length; i++) {
            x[i] = Math.random() * 10 - 6.0;
        }

        double[][] d = new double[15][15];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                double a = x[j];
                switch ((int) k[i]) {
                    case 9:
                        // (e^((x)^((2/3+x)/x))*(1-2/(1/2-cos(x))))^(arctan(e^(-abs(x))))
                        d[i][j] = Math.pow(
                            Math.pow(Math.E, Math.pow(a, (2 / 3 + a) / a))
                                * (1 - 2 / (1 / 2 - Math.cos(a))),
                            Math.atan(Math.pow(Math.E, -Math.abs(a)))
                        );
                        break;
                    case 5:
                    case 7:
                    case 11:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                        // e^((1/2/(x)^(x/2))^(tan(x)))
                        d[i][j] = Math.pow(Math.E, Math.pow(1 / 2 / Math.pow(a, a / 2), Math.tan(a)));
                        break;
                    default:
                        // arcsin(e^(root(3)(-(2*(pi/(abs(x)+1))^(x))^((cos(x)+1)^3))))
                        d[i][j] = Math.asin(
                            Math.pow(
                                Math.E,
                                Math.pow(
                                    Math.pow(
                                        -(2 * Math.pow(Math.PI / (Math.abs(a) + 1), a)),
                                        Math.pow(Math.cos(a) + 1, 3)
                                    ),
                                    1 / 3
                                )
                            )
                        );
                }
            }
        }

        for (double[] array : d) {
            for (double item : array) {
                System.out.printf("%.4g ", item);
            }
            System.out.println();
        }
    }
}
