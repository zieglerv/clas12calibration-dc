/**
 * Main.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */ 

package eu.mihosoft.vrl.v3d;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Main {

    public static void main(String[] args) throws IOException {

        // we use cube and sphere as base geometries
        CSG cube = new Cube(2).toCSG();
        CSG sphere = new Sphere(1.25).toCSG();

        // perform union, difference and intersection
        CSG cubePlusSphere = cube.union(sphere);
        CSG cubeMinusSphere = cube.difference(sphere);
        CSG cubeIntersectSphere = cube.intersect(sphere);
        
        // translate geometries to prevent overlapping 
        CSG union = cube.
                union(sphere.transformed(Transform.unity().translateX(3))).
                union(cubePlusSphere.transformed(Transform.unity().translateX(6))).
                union(cubeMinusSphere.transformed(Transform.unity().translateX(9))).
                union(cubeIntersectSphere.transformed(Transform.unity().translateX(12)));
        
        // save union as stl
        
            FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
          FileUtil.write(Paths.get("sample.stl"), new ServoMount().toCSG().transformed(Transform.unity().scale(1.0)).toStlString());
   
//                 FileUtil.write(Paths.get("sample.stl"), RaspberryPiMount.boardAndPegs().transformed(Transform.unity().scale(1.0)).toStlString());
                 
                 
//                  FileUtil.write(Paths.get("sample.stl"), Peg.peg().transformed(Transform.unity().scale(1.0)).toStlString());

    
    }
}
