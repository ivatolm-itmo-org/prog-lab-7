with open("data.csv") as file:
    content = file.readlines()


result = []
for i, line in enumerate(content[1:]):
    row = line.strip().split(',')

    date = row[2]

    d, m, y = date.split('/')
    date = f"{d}.{m}.2018"

    row[2] = date

    result.append(row)

print(result)

with open("new_data.csv", 'w') as file:
    file.write(content[0])

    for line in result:
        for item in line:
            file.write(item + ',')
        file.write('\n')
