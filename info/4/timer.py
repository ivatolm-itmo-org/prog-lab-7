import time

from converter import convert
from converter_lib import convert as convert_lib

ITERATIONS = 100

time_start = time.time()
for _ in range(ITERATIONS):
    convert()
time_elapsed = time.time() - time_start
print(f"Native: {time_elapsed}")

time_regex_start = time.time()
for _ in range(ITERATIONS):
    convert(regex=True)
time_regex_elapsed = time.time() - time_regex_start
print(f"Regex: {time_regex_elapsed}")

lib_time_start = time.time()
for _ in range(ITERATIONS):
    convert_lib()
lib_time_elapsed = time.time() - lib_time_start
print(f"Lib: {lib_time_elapsed}")
