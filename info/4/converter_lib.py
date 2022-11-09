from json2xml import json2xml
from json2xml.utils import readfromstring, readfromjson


def convert():
    with open("res/output.xml", 'w') as output_file:
        data = readfromjson("res/test_xml.json")
        content = json2xml.Json2xml(data).to_xml()

        for line in content:
            output_file.write(line)
