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

SITE_URL_MEMBERS = 'https://www.bluetooth.com/specifications/assigned-numbers/16-bit-uuids-for-members'
SITE_URL_SDOS = 'https://www.bluetooth.com/specifications/assigned-numbers/16-bit-uuids-for-sdos'
SITE_URL_GATT_SERVICES = 'https://www.bluetooth.com/specifications/gatt/services'
RE_CONTENT_JSON = re.compile(r"\s*data:\s*(.*),\s*")

RE_CONTENT_GATT = re.compile(r"<tbody>(.*)</tbody>", re.DOTALL)
RE_BLOCK = re.compile(r"<tr>(.*?)</tr>", re.DOTALL)
RE_ELEMENT = re.compile(r"<td[^>]*><a[^>]*>(.*?)</a></td>.*?<td[^>]*>(.*?)</td>.*?<td[^>]*>(.*?)</td>.*?<td[^>]*>(.*?)</td>", re.DOTALL)

def parse_site_members_data(site_data):
    companies = {}
    data = RE_CONTENT_JSON.search(site_data).group(1)
    data_json = json.loads(data)
    for line in data_json:
        id, name = int(line[0]), line[2]
        companies[id] = name
    return companies

def parse_site_sdos_data(site_data):
    companies = {}
    data = RE_CONTENT_JSON.search(site_data).group(1)
    data_json = json.loads(data)
    for line in data_json:
        id, name = int(line[0], base=0), line[1]
        companies[id] = name
    return companies

def parse_gatt_services(site_data):
    services = {}
    data = RE_CONTENT_GATT.search(site_data).group(1)
    match_iter = RE_BLOCK.finditer(data)
    for token_match in match_iter:
        element = RE_ELEMENT.search(token_match.group(0))
        if element is None:
            continue
        id = int(element.group(3), base=0)
        name = element.group(1)
        services[id] = name
    return services

def main():
    # Member site
    response = urllib.request.urlopen(SITE_URL_MEMBERS)
    data = response.read().decode("utf-8")
    result = parse_site_members_data(data)
    # SDOS site
    response = urllib.request.urlopen(SITE_URL_SDOS)
    data = response.read().decode("utf-8")
    result.update(parse_site_sdos_data(data))
    # GATT Services
    response = urllib.request.urlopen(SITE_URL_GATT_SERVICES)
    data = response.read().decode("utf-8")
    result.update(parse_gatt_services(data))
    print(json.dumps(result))


if __name__ == "__main__":
    main()