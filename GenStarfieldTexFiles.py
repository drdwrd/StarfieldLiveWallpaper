#!/usr/bin/env python

import os
import sys
import errno
import subprocess
import argparse
import zipfile

def make_dir(dirName):
    try:
        print("mkdir(): %s" %dirName)
        os.makedirs(dirName)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise
        else:
            print("mkdir(): directory already exists")

argParser = argparse.ArgumentParser()
argParser.add_argument('-b', '--background', action='store')
argParser.add_argument('-s', '--starsprite', action='store')
argParser.add_argument('-c', '--cloudsprite', action='store')
argParser.add_argument('-o', '--output', action='store', required=True)
args = vars(argParser.parse_args())
path = os.getcwd()
print("working directory: %s" %path)
outputName = args['output']
make_dir(outputName)
make_dir("%s/astc" %outputName)
make_dir("%s/etc2" %outputName)
make_dir("%s/etc" %outputName)
make_dir("%s/png" %outputName)
print("encoding files...")
if args['background']:
    inputName = args['background']
    backgroundOutputName = os.path.splitext(os.path.basename(args['background']))[0]
    subprocess.call("PVRTexToolCLI -i %s -o %s/astc/%s.ktx -m -f ASTC_8x8 -q astcmedium" %(inputName, outputName, backgroundOutputName), shell=True)
    subprocess.call("PVRTexToolCLI -i %s -o %s/etc2/%s.ktx -m -f ETC2_RGB -q etcfast" %(inputName, outputName, backgroundOutputName), shell=True)
    subprocess.call("PVRTexToolCLI -i %s -o %s/etc/%s.ktx -m -f ETC1 -q etcfast" %(inputName, outputName, backgroundOutputName), shell=True)
    subprocess.call("convert -resize 50%% %s %s/png/%s.png" %(inputName, outputName, backgroundOutputName), shell=True)

if args['starsprite']:
    inputName = args['starsprite']
    starspriteOutputName = os.path.splitext(os.path.basename(args['starsprite']))[0]
    subprocess.call("PVRTexToolCLI -i %s -o %s/astc/%s.ktx -m -f ASTC_8x8 -q astcmedium" %(inputName, outputName, starspriteOutputName), shell=True)
    subprocess.call("PVRTexToolCLI -i %s -o %s/etc2/%s.ktx -m -f ETC2_RGBA -q etcfast" %(inputName, outputName, starspriteOutputName), shell=True)
    subprocess.call("convert -resize 50%% %s %s/png/%s.png" %(inputName, outputName, starspriteOutputName), shell=True)

if args['cloudsprite']:
    inputName = args['cloudsprite']
    cloudspriteOutputName = os.path.splitext(os.path.basename(args['cloudsprite']))[0]
    subprocess.call("PVRTexToolCLI -i %s -o %s/astc/%s.ktx -m -f ASTC_8x8 -q astcmedium" %(inputName, outputName, cloudspriteOutputName), shell=True)
    subprocess.call("PVRTexToolCLI -i %s -o %s/etc2/%s.ktx -m -f ETC2_RGBA -q etcfast" %(inputName, outputName, cloudspriteOutputName), shell=True)
    subprocess.call("convert -resize 50%% %s %s/png/%s.png" %(inputName, outputName, cloudspriteOutputName), shell=True)

print("compressing packages...")
with zipfile.ZipFile('%s/%s_astc.zip' %(outputName, outputName), 'w', zipfile.ZIP_DEFLATED) as z:
    if args['background']:
        z.write('%s/astc/%s.ktx' %(outputName, backgroundOutputName), '%s.ktx' %backgroundOutputName)
    if args['starsprite']:
        z.write('%s/astc/%s.ktx' %(outputName, starspriteOutputName), '%s.ktx' %starspriteOutputName)
    if args['cloudsprite']:
        z.write('%s/astc/%s.ktx' %(outputName, cloudspriteOutputName), '%s.ktx' %cloudspriteOutputName)

with zipfile.ZipFile('%s/%s_etc2.zip' %(outputName, outputName), 'w', zipfile.ZIP_DEFLATED) as z:
    if args['background']:
        z.write('%s/etc2/%s.ktx' %(outputName, backgroundOutputName), '%s.ktx' %backgroundOutputName)
    if args['starsprite']:
        z.write('%s/etc2/%s.ktx' %(outputName, starspriteOutputName), '%s.ktx' %starspriteOutputName)
    if args['cloudsprite']:
        z.write('%s/etc2/%s.ktx' %(outputName, cloudspriteOutputName), '%s.ktx' %cloudspriteOutputName)

with zipfile.ZipFile('%s/%s_etc.zip' %(outputName, outputName), 'w', zipfile.ZIP_DEFLATED) as z:
    if args['background']:
        z.write('%s/etc/%s.ktx' %(outputName, backgroundOutputName), '%s.ktx' %backgroundOutputName)
    if args['starsprite']:
        z.write('%s/png/%s.png' %(outputName, starspriteOutputName), '%s.png' %starspriteOutputName)
    if args['cloudsprite']:
        z.write('%s/png/%s.png' %(outputName, cloudspriteOutputName), '%s.png' %cloudspriteOutputName)

with zipfile.ZipFile('%s/%s_png.zip' %(outputName, outputName), 'w', zipfile.ZIP_DEFLATED) as z:
    if args['background']:
        z.write('%s/png/%s.png' %(outputName, backgroundOutputName), '%s.png' %backgroundOutputName)
    if args['starsprite']:
        z.write('%s/png/%s.png' %(outputName, starspriteOutputName), '%s.png' %starspriteOutputName)
    if args['cloudsprite']:
        z.write('%s/png/%s.png' %(outputName, cloudspriteOutputName), '%s.png' %cloudspriteOutputName)

