import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

Future<int> sendData(array, ip, port) async {
  var url = '${'http://' + ip}:$port/params'; // replace with your endpoint
  var data = {
    'data': array,
  };

  var response = await http.post(
    Uri.parse(url),
    headers: <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
    },
    body: jsonEncode(data),
  );

  if (response.statusCode == 200) {

    print('Data sent successfully');
    return 1;
  } else {

    print('Failed to send data. Status code: ${response.statusCode}');
    return 0;
  }
}