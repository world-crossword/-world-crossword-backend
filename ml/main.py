import enum
import itertools
import math
import numpy as np
import random
import os
import pandas as pd
import string    
import sys

f = open('./w.txt', 'r')

words = [s.strip() for s in f.readlines()]

random.shuffle(words)

words = words[:210]

index = 0

wordList = {
    'word': [],
    'row': [],
    'col': [],
    'direction': [],
    'completion': []
}

def random_string(length):
    result = ''.join((random.choice(string.ascii_lowercase) for x in range(length)))
    return result 

@enum.unique
class Direction(enum.Enum):
    ACROSS = enum.auto()
    DOWN = enum.auto()

    def __str__(self):
        return("ACROSS" if self is Direction.ACROSS else "DOWN")

    def get_deltas(self):
        delta_r = int(self == Direction.DOWN)
        delta_c = int(self == Direction.ACROSS)
        return(delta_r, delta_c)

    @staticmethod
    def random():
        return random.choice(list(Direction))


class GridWord:
    def __init__(self, word: str, r: int, c: int, direction: Direction):
        if not isinstance(word, str):
            raise TypeError("word must be a string")
        if not (isinstance(r, int) and isinstance(c, int) and r >= 0 and c >= 0):
            raise ValueError("Row and column positions must be positive integers")
        if not isinstance(direction, Direction):
            raise TypeError("Direction must be an enum of type Direction")

        self.word = word.upper()
        self.r1 = r
        self.c1 = c
        self.direction = direction
        self.delta_r, self.delta_c = self.direction.get_deltas()

        self.__len = len(self.word)
        self.r2 = self.r1 + (self.__len - 1)* self.delta_r
        self.c2 = self.c1 + (self.__len - 1)* self.delta_c

    def __str__(self):
        return(f"{self.word}, ({self.r1}, {self.c1}) -- ({self.r2}, {self.c2}), {self.direction}")

    def __len__(self):
        return(self.__len)

    def __contains__(self, item):
        if isinstance(item, str):
            return(item in self.word)
        elif isinstance(item, tuple) and len(item) == 2 and isinstance(item[0], int) and isinstance(item[1], int):
            return(self.r1 <= item[0] and item[0] <= self.r2 and
                   self.c1 <= item[1] and item[1] <= self.c2)
        else:
            raise TypeError("'in <GridWord>' requires string or coordinate pair as left operand")

    def __getitem__(self, item):
        try:
            return(self.word[item])
        except: 
            raise

    def intersects(self, other):
        if not isinstance(other, GridWord):
            raise TypeError("Intersection is only defined for two GridWords")
        if self.direction == other.direction:
            raise ValueError("Intersection is only defined for GridWords placed in different directions")

        for idx1, letter1 in enumerate(self.word):
            for idx2, letter2 in enumerate(other.word):
                rr1 = self.r1 + idx1*self.delta_r
                cc1 = self.c1 + idx1*self.delta_c
                rr2 = other.r1 + idx2*self.delta_c 
                cc2 = other.c1 + idx2*self.delta_r
                if letter1 == letter2 and rr1 == rr2 and cc1 == cc2:
                    return(True)
        return(False)

    def overlaps(self, other):
        if not isinstance(other, GridWord):
            raise TypeError("Overlap check is only defined for two GridWords")
        if self.direction == other.direction:
            return((self.r1, self.c1) in other or (other.r1, other.c1) in self)

        for idx, letter in enumerate(self.word):
            rr = self.r1 + idx*self.delta_r
            cc = self.c1 + idx*self.delta_c
            if (rr, cc) in other:
                return(True)
        return(False)


    def adjacent_to(self, other):
        if not isinstance(other, GridWord):
            raise TypeError("Adjacency is only defined for two GridWords")
        if self.direction != other.direction:
            return(False)
        for delta in [-1, 1]:
            for idx in range(self.__len):
                r = self.r1 + idx*self.delta_r + delta*self.delta_c
                c = self.c1 + idx*self.delta_c + delta*self.delta_r
                if (r, c) in other:
                    return(True)

            if delta == -1:
                r = self.r1 + delta * self.delta_r
                c = self.c1 + delta * self.delta_c
            elif delta == 1:
                r = self.r2 + delta * self.delta_r
                c = self.c2 + delta * self.delta_c
            if (r, c) in other:
                return(True)
        return(False) 



class Grid:
    def __init__(self, num_rows = 50, num_cols = 50):
        self.num_rows = num_rows
        self.num_cols = num_cols
        self.grid = np.full([self.num_rows, self.num_cols], "")
        self.grid_words = []

    def __str__(self):
        s = ""


        for i in range(self.num_rows):
            for j in range(self.num_cols):
                s += self.grid[i][j] if self.grid[i][j] != "" else "-"
            s += "\n"
        return(s)

    def __approximate_center(self):
        center = (math.floor(self.num_rows / 2), math.floor(self.num_cols / 2))
        return(center)

    def __insert_word(self, grid_word):
        if not isinstance(grid_word, GridWord):
            raise TypeError("Only GridWords can be inserted into the Grid")
        delta_r, delta_c = grid_word.direction.get_deltas()
        for idx, letter in enumerate(grid_word.word):
            self.grid[grid_word.r1 + idx*delta_r, grid_word.c1 + idx*delta_c] = letter
        self.grid_words.append(grid_word)

    def __word_fits(self, word: str, r: int, c: int, d: Direction):
        if ((d == Direction.DOWN and r + len(word) >= self.num_rows) or
            (d == Direction.ACROSS and c + len(word) >= self.num_cols)):
            return(False)
        grid_word = GridWord(word, r, c, d)


        check = False
        for gw in self.grid_words:
            if grid_word.adjacent_to(gw):
                return(False)
            if grid_word.overlaps(gw):
                if d == gw.direction:
                    return(False)
                elif not grid_word.intersects(gw):
                    return(False)
                else:
                    check = True
            else:
                pass
        return(check)      




    def __scan_and_insert_word(self, word):
        if not isinstance(word, str):
            raise TypeError("Only strings can be inserted into the puzzle by scanning")

        global wordList
        global index
        index += 1
        if len(self.grid_words) == 0:
            r, c = self.__approximate_center()
            d = Direction.random()
            self.__insert_word(GridWord(word, r, c, d))
            print(index, word, r, c, d)
            wordList['word'].append(word)
            wordList['row'].append(r)
            wordList['col'].append(c)
            wordList['direction'].append(d)
            wordList['completion'].append(0)
            return(None)
        for d, r, c in itertools.product(list(Direction), range(self.num_rows), range(self.num_cols)):
            if self.__word_fits(word, r, c, d):
                grid_word = GridWord(word, r, c, d)
                self.__insert_word(grid_word)
                print(index, word, r, c, d)
                wordList['word'].append(word)
                wordList['row'].append(r)
                wordList['col'].append(c)
                wordList['direction'].append(d)
                wordList['completion'].append(0)
                break

    def scan_and_insert_all_words(self, words):
        for word in words:
            self.__scan_and_insert_word(word)


    def __randomly_insert_word(self, word):
        if not isinstance(word, str):
            raise TypeError("Only strings can be randomly inserted into the puzzle")
        if len(self.grid_words) == 0:
            self.__insert_word(GridWord(word, *self.__approximate_center(), Direction.random()))
            return(None)
        num_iterations = 0
        while num_iterations <= 10000:
            rand_r = random.randint(0, self.num_rows - 1)
            rand_c = random.randint(0, self.num_cols - 1)
            d = Direction.random()
            if self.__word_fits(word, rand_r, rand_c, d):
                grid_word = GridWord(word, rand_r, rand_c, d)
                self.__insert_word(grid_word)
                break
            num_iterations += 1


    def crop(self):
        cropped_grid = Grid(50, 50)
        for grid_word in self.grid_words:
            cropped_word = GridWord(grid_word.word, grid_word.r1, grid_word.c1, grid_word.direction)
            cropped_grid.__insert_word(cropped_word)
        return(cropped_grid)




random.seed(1)
g = Grid()
g.scan_and_insert_all_words(words)

filename = random_string(10)

print(g.crop())

print(wordList)

dataList = pd.DataFrame(data=wordList)
dataList.to_csv(sys.argv[1] + '.csv')

save = open(sys.argv[1] + '.txt', 'w')
save.write(str(g.crop()))
save.close()