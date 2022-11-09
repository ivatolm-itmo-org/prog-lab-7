from parser import parse, lex


def generate_xml(obj):
    def generate_object(obj, nesting=0):
        FMT_STR = ' ' * nesting + "{}" + '\n'

        for key, item in obj.items():
            content.append(FMT_STR.format(f"<{key}>"))

            if isinstance(item, dict):
                generate_object(item, nesting=nesting+NESTING)

            elif isinstance(item, list):
                generate_array(item, nesting=nesting+NESTING)

            else:
                content.append(FMT_STR.format(' ' * NESTING + str(item)))

            content.append(FMT_STR.format(f"</{key}>"))


    def generate_array(array, nesting=0):
        FMT_STR = ' ' * nesting + "{}" + '\n'

        for item in array:
            content.append(FMT_STR.format(f"<{ELEM}>"))

            if isinstance(item, dict):
                generate_object(item, nesting=nesting+NESTING)

            elif isinstance(item, list):
                generate_array(item, nesting=nesting+NESTING)

            else:
                content.append(FMT_STR.format(' ' * NESTING + str(item)))

            content.append(FMT_STR.format(f"</{ELEM}>"))

    ELEM = "element"
    NESTING = 2

    content = ['<?xml version="1.0" encoding="UTF-8"?>\n']
    content.append("<data>\n")
    if isinstance(obj, dict):
        generate_object(obj, nesting=2)
    elif isinstance(obj, list):
        generate_array(obj, nesting=2)
    content.append("</data>\n")

    return content


def generate_csv(obj):
    content = []

    title_row = ""
    columns = tuple(obj[0].keys())
    for i, column_name in enumerate(columns):
        if isinstance(column_name, str):
            title_row += f'"{column_name}"'

        if i < len(columns) - 1:
            title_row += ','

    content.append(title_row + '\n')

    for row in obj:
        row_str = ""
        for i, item in enumerate(row.values()):
            if isinstance(item, str):
                row_str += f'"{item}"'
            else:
                row_str += f'{item}'

            if i < len(row) - 1:
                row_str += ','

        content.append(row_str + '\n')

    return content


def convert(regex=False):
    with open("res/output.xml", 'w') as output_file:
        with open("res/test_xml.json") as file:
            content = file.read()

        json = parse(lex(content, regex=regex))[0]
        content = generate_xml(json)

        for line in content:
            output_file.write(line)

    # with open("res/output.csv", 'w') as output_file:
    #     with open("res/test_csv.json") as file:
    #         content = file.read()

    #     json = parse(lex(content))[0]
    #     content = generate_csv(json)

    #     for line in content:
    #         output_file.write(line)

convert(regex=True)
