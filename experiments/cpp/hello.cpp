#include <iostream>
using namespace std;

double square_value(double x) {
    return x * x;
}

void print_square(double x) {
    cout << "The square value of " << x << " is " << square_value(x) << "\n";
}

int main() {
    print_square(1.234);
}

