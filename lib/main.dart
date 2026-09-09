import 'package:flutter/material.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Video Editor Pro 2050',
      home: Scaffold(
        appBar: AppBar(title: const Text('Video Editor Pro 2050')),
        body: const Center(
          child: Text('APK Successfully Built!', style: TextStyle(fontSize: 24)),
        ),
      ),
    );
  }
}
