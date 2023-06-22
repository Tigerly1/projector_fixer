import 'dart:async';
import 'dart:ffi';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:flutter/services.dart';
import 'package:image/image.dart' as img;
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:sensoryczne/network/connection.dart';


late List<CameraDescription> cameras;

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  cameras = await availableCameras();
  runApp(CameraApp());
}

class CameraApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Projection fixer',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: CameraScreen(cameras),
    );
  }
}

class CameraScreen extends StatefulWidget {
  final List<CameraDescription> cameras;

  const CameraScreen(this.cameras);

  @override
  CameraScreenState createState() {
    return CameraScreenState();
  }
}

class CameraScreenState extends State<CameraScreen> {
  CameraController? controller;
  Timer? timer;
  static const platform = MethodChannel('samples.flutter.dev/camera');
  late CameraImage latestImage;
  var isInitialized = false;
  BuildContext? dialogContext;
  final TextEditingController _ipController = TextEditingController();
  final TextEditingController _portController = TextEditingController();
  var ip = "10.0.2.2";
  var port = "5000";

  @override
  void initState() {
    super.initState();
    _requestPermission();
     WidgetsBinding.instance.addPostFrameCallback((_) {
      _showIPAndPortDialog();
    });

    if (widget.cameras.isEmpty) {
      print('No camera available');
    } else {
      controller = CameraController(widget.cameras[0], ResolutionPreset.medium);
      controller?.initialize().then((_) {
    if (!mounted) {
      return;
    }
    setState(() {});
    
    

    if (controller?.value.isInitialized == true) {
      controller?.startImageStream((simg) {
        latestImage = simg;
        isInitialized = true;
      }).catchError((e) {
        print('Error starting image stream: $e');
      });

      timer = Timer.periodic(Duration(milliseconds: 5000), (Timer t) async {
        if (controller!.value.isStreamingImages && isInitialized) {
          await sendLatestFrame(controller!);
        }
      });
    } else {
      print('Error initializing camera');
    }
  }).catchError((e) {
    print('Error initializing camera: $e');
  });
    }
  }
  _requestPermission() async {
    if (await Permission.storage.request().isGranted) {
      // Either the permission was already granted before or the user just granted it.
      print('Storage permission granted');
    }
  }

  Future<void> sendLatestFrame(CameraController cnt) async {
    print("WELL");
    await cnt.stopImageStream();
    String pathBase = '${(await getTemporaryDirectory()).path}/${DateTime.now().toIso8601String()}';
    var imagePath = '$pathBase.png';
    XFile image = await cnt!.takePicture();
    image.saveTo(imagePath!);
    await File(imagePath!).exists();

    final List<dynamic> result =  await platform.invokeMethod('sendFrame', imagePath);
    List<double> array = result.cast<double>();
    print(array);
    int isConnected = await sendData(array, ip ,port);
    if(isConnected == 1){
      if(dialogContext != null) {
        Navigator.of(dialogContext!).pop();
        showOverlay(context, 'Connected successfully');
        dialogContext = null;
      }
    }
    // Start the image stream again
    cnt.startImageStream((simg) {
      latestImage = simg;
      isInitialized = true;
    }).catchError((e) {
      print('Error restarting image stream: $e');
    });
  }

// Updated timer

  void showOverlay(BuildContext context, String message) {
    OverlayState overlayState = Overlay.of(context)!;
    OverlayEntry overlayEntry = OverlayEntry(
      builder: (context) => Positioned(
        bottom: 50.0,
        child: Material(
          color: Colors.transparent,
          child: Container(
            alignment: Alignment.center,
            width: MediaQuery.of(context).size.width,
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 20),
              padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 20),
              decoration: BoxDecoration(
                color: Colors.grey[800],
                borderRadius: BorderRadius.circular(10),
              ),
              child: Text(
                message,
                style: TextStyle(color: Colors.white),
              ),
            ),
          ),
        ),
      ),
    );

    overlayState.insert(overlayEntry);

    // Remove the overlay entry after 4 seconds.
    Future.delayed(Duration(seconds: 4)).then((value) => overlayEntry.remove());
  }


  Future<void> _showIPAndPortDialog() async {
    return showDialog<void>(
      context: context,
      barrierDismissible: false, // user must tap button!
      builder: (BuildContext context) {
        dialogContext = context;
        return AlertDialog(
          title: Text('Enter IP and Port'),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                TextFormField(
                  controller: _ipController,
                  decoration: InputDecoration(
                    hintText: 'Enter local pc ip',
                    labelText: "10.0.2.2"
                  ),
                ),
                TextFormField(
                  controller: _portController,
                  decoration: InputDecoration(
                    hintText: 'Enter Port',
                      labelText: "5000"
                  ),
                ),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: Text('Submit'),
              onPressed: () {
                //Navigator.of(context).pop();
                ip = _ipController.text;
                port = _portController.text;
                // Here you can use _ipController.text and _portController.text to get IP and Port values
              },
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    if (controller == null || !controller!.value.isInitialized) {
      return Container(
        decoration: const BoxDecoration(
          image: DecorationImage(
            image: AssetImage('assets/images/wall.png'),
            fit: BoxFit.fill,
          ),
        ),
      );
    }
    return AspectRatio(
      aspectRatio: controller!.value.aspectRatio,
      child: CameraPreview(controller!),
    );
  }

  @override
  void dispose() {
    timer?.cancel();
    controller?.dispose();
    super.dispose();
  }
}
