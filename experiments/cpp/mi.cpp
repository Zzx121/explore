#include <bits/stdc++.h>
using namespace std;
int main(void)
{
	int a, b;
	cout << "�����������:";
	cin >> a;
	cout << "������������:";
	cin >> b;
	srand(static_cast<unsigned int>(time(nullptr)));
	int starty = rand() % b + 2;
	int endy;
	int startx = rand() % a + 2;
	int endx;
	int numbery = b / 2;
	int way;
	vector<vector<tuple<int, int, int>>> arr(b + 2, vector<tuple<int, int, int>>(a + 2, make_tuple(0, 0, 0)));

	for (int i = 0; i <= 1000; i++)
	{
		endy = rand() % b + 2;
		if (starty != endy && abs(starty - endy) > b / 2)
		{
			break;
		}
	}
	if (starty == endy || abs(starty - endy) < b / 2)
	{
		if (starty + numbery > b + 1)
		{
			endy = starty - numbery;
		}
		else if (starty + numbery <= b + 1)
		{
			endy = starty + numbery;
		}
	}
	for (int j = 0; j <= 1000; j++)
	{
		endx = rand() % b + 2;
		if (startx < endx && abs(startx - endx) > a / 2)
		{
			break;
		}
	}
	if (startx > endx || abs(startx - endx) < a / 2)
	{
		startx = 2;
		endx = a + 1;
	}
	for (;;)
	{
		way = rand() % a + 2;
		if (startx < way && way < endx)
		{
			break;
		}
	}
	for (int y = 1; y <= b + 2; y++)
	{
		for (int x = 1; x <= a + 2; x++)
		{
			arr[y - 1][x - 1] = make_tuple(x - 1, y - 1, 0);
			int o = rand() % 2;
			if (x == startx && y == starty)
			{
				get<2>(arr[y - 1][x - 1]) = 0;
				cout << "��";
			}
			else if (x == endx && y == endy)
			{
				get<2>(arr[y - 1][x - 1]) = 2;
				cout << "��";
			}
			else if (starty <= endy)
			{
				if (x >= startx && x <= way && y == starty)
				{
					get<2>(arr[y - 1][x - 1]) = 1;
					cout << " ";
				}
				else if (x == way && y > starty && y <= endy)
				{
					get<2>(arr[y - 1][x - 1]) = 1;
					cout << " ";
				}
				else if (x > way && x <= endx && y == endy)
				{
					get<2>(arr[y - 1][x - 1]) = 1;
					cout << " ";
				}
				else if (x == 1 && y == 1)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (x == 1 && y == b + 2)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (x == a + 2 && y == 1)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (x == a + 2 && y == b + 2)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (x == 1 || x == a + 2)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "|";
				}
				else if (y == 1 || y == b + 2)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (o == 1)
				{
					get<2>(arr[y - 1][x - 1]) = 1;
					cout << " ";
				}
				else
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "*";
				}
			}
			else if (starty > endy)
			{
				if (x >= startx && x <= way && y == starty)
				{
					get<2>(arr[y - 1][x - 1]) = 1;
					cout << " ";
				}
				else if (x == way && y < starty && y >= endy)
				{
					get<2>(arr[y - 1][x - 1]) = 1;
					cout << " ";
				}
				else if (x > way && x <= endx && y == endy)
				{
					get<2>(arr[y - 1][x - 1]) = 1;
					cout << " ";
				}
				else if (x == 1 && y == 1)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (x == 1 && y == b + 2)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (x == a + 2 && y == 1)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (x == a + 2 && y == b + 2)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (x == 1 || x == a + 2)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "|";
				}
				else if (y == 1 || y == b + 2)
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "��";
				}
				else if (o == 1)
				{
					get<2>(arr[y - 1][x - 1]) = 1;
					cout << " ";
				}
				else
				{
					get<2>(arr[y - 1][x - 1]) = 3;
					cout << "*";
				}
			}
		}
		cout << endl;
	}
	for (int y = 1; y <= b + 2; y++)
	{
		for (int x = 1; x <= a + 2; x++)
		{
			cout << "(" << get<0>(arr[y - 1][x - 1]) << "," << get<1>(arr[y - 1][x - 1]) << "," << get<2>(arr[y - 1][x - 1]) << ") ";
		}
		cout << endl;
	}
}
