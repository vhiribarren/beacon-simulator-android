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

SITE_URL = 'https://www.bluetooth.com/specifications/assigned-numbers/generic-access-profile'
RE_CONTENT = re.compile(r"<tbody>(.*)</tbody>", re.DOTALL)
RE_BLOCK = re.compile(r"<tr(?:.*?)>(.*?)</tr>", re.DOTALL)
RE_ELEMENT = re.compile(r"<td[^>]*>(.*?)</td>.*?<td[^>]*>&lt;&lt;(.*?)&gt;&gt;</td>.*?<td[^>]*>(.*?)</td>", re.DOTALL)

def parse_site_data(site_data):
    gap_type = {}
    data = RE_CONTENT.search(site_data).group(1)
    match_iter = RE_BLOCK.finditer(data)
    for token_match in match_iter:
        element = RE_ELEMENT.search(token_match.group(0))
        if element is None:
            continue
        id = int(element.group(1), base=0)
        name = element.group(2)
        gap_type[id] = name
    return gap_type


def main():
    response = urllib.request.urlopen(SITE_URL)
    data = response.read().decode("utf-8")
    result = parse_site_data(data)
    print(json.dumps(result))


if __name__ == "__main__":
    main()