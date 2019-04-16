#!/usr/bin/env python3
# -*- coding: utf-8 -*-
#
# Copyright (c) 2016 Vincent Hiribarren
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.


import json
import re
import urllib.request

SITE_URL = 'https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers'
RE_CONTENT = re.compile(r"\s*data:\s*(.*),\s*")

def parse_site_data(site_data):
    companies = {}
    data = RE_CONTENT.search(site_data).group(1)
    data_json = json.loads(data)
    for line in data_json:
        id, name = int(line[0]), line[2]
        companies[id] = name
    companies[65535] = "Special value / Tests"
    return companies


def main():
    response = urllib.request.urlopen(SITE_URL)
    data = response.read().decode("utf-8")
    result = parse_site_data(data)
    print(json.dumps(result))


if __name__ == "__main__":
    main()