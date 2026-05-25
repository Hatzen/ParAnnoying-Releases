package de.hartz.software.parannoying.offline.helper.security.experimental;

import java.math.BigInteger;

public class HiddenFieldEquations {

    // Method to perform modular inverse using Extended Euclidean Algorithm
    public static BigInteger modInverse(BigInteger a, BigInteger p) {
        BigInteger[] result = extendedGCD(a, p);
        return result[1].mod(p);
    }

    // Extended Euclidean Algorithm to find gcd and the inverse
    private static BigInteger[] extendedGCD(BigInteger a, BigInteger b) {
        BigInteger[] result = new BigInteger[2];
        if (b.equals(BigInteger.ZERO)) {
            result[0] = a;
            result[1] = BigInteger.ONE;
            return result;
        }
        BigInteger[] temp = extendedGCD(b, a.mod(b));
        result[0] = temp[0];
        result[1] = temp[2].subtract(a.divide(b).multiply(temp[1]));
        return result;
    }

    // Solve a system of equations Ax = B (mod p)
    public static BigInteger[] solveSystem(BigInteger[][] A, BigInteger[] B, BigInteger p) {
        int n = A.length;
        BigInteger[] X = new BigInteger[n];
        for (int i = 0; i < n; i++) {
            X[i] = BigInteger.ZERO;
        }

        // Gaussian elimination in modular arithmetic (mod p)
        for (int i = 0; i < n; i++) {
            BigInteger pivot = A[i][i];
            if (pivot.equals(BigInteger.ZERO)) {
                // Pivot must not be zero, otherwise the system has no solution
                System.out.println("No solution due to zero pivot");
                return null;
            }
            // Make pivot element 1 by multiplying by its modular inverse
            pivot = modInverse(pivot, p);
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j].multiply(pivot).mod(p);
            }
            B[i] = B[i].multiply(pivot).mod(p);

            // Eliminate all other entries in column i
            for (int j = i + 1; j < n; j++) {
                if (!A[j][i].equals(BigInteger.ZERO)) {
                    BigInteger factor = A[j][i];
                    for (int k = 0; k < n; k++) {
                        A[j][k] = A[j][k].subtract(factor.multiply(A[i][k])).mod(p);
                    }
                    B[j] = B[j].subtract(factor.multiply(B[i])).mod(p);
                }
            }
        }

        // Back substitution to find the solution
        for (int i = n - 1; i >= 0; i--) {
            X[i] = B[i];
            for (int j = i + 1; j < n; j++) {
                X[i] = X[i].subtract(A[i][j].multiply(X[j])).mod(p);
            }
        }

        return X;
    }

    public static void main(String[] args) {
        // Example: System of equations over GF(p), p = 7
        BigInteger p = BigInteger.valueOf(7);

        // Coefficients of the system (A matrix)
        BigInteger[][] A = {
            {BigInteger.valueOf(1), BigInteger.valueOf(2)},
            {BigInteger.valueOf(3), BigInteger.valueOf(4)}
        };

        // Constants on the right-hand side (B vector)
        BigInteger[] B = {BigInteger.valueOf(5), BigInteger.valueOf(6)};

        // Solve the system
        BigInteger[] solution = solveSystem(A, B, p);

        if (solution != null) {
            System.out.println("Solution: ");
            for (BigInteger x : solution) {
                System.out.print(x + " ");
            }
        }
    }
}
