msg = [int(item) for item in input()]

if len(msg) != 7:
    print("Incorrect message length")
    exit(-1)

if msg.count(0) + msg.count(1) != 7:
    print("Message contains unsupported characters")
    exit(-1)

r1, r2, i1, r3, i2, i3, i4 = msg

s1 = (r1 + i1 + i2 + i4) % 2
s2 = (r2 + i1 + i3 + i4) % 2
s3 = (r3 + i2 + i3 + i4) % 2

s = f"{s3}{s2}{s1}"

if int(s) == 0:
    print("No errors found")
    exit(0)

error_bit = int(s, 2)
msg[error_bit - 1] = int(not msg[error_bit - 1])

print(f"Error in bit {error_bit}")
print(f"Corrected message: {''.join([str(item) for item in msg])}")
