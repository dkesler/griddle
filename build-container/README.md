To enable official, portable, repeatable builds, we provide a docker-based build process.  This is largely necessary due to the need of the testing process to invoke a thrift binary that is available on the path.  If you have a thrift binary on your path, you should be able to invoke gradle builds directly.  Otherwise, you can follow the instructions below.

### Creating the build container
From the root of the griddle repo, execute

`docker build build-container -t griddle-build`

This process will take several minutes, but will not need to be repeated unless the build container Dockerfile changes

### Executing builds
From the root of the griddle repo, execute

`docker run -v $(pwd):/src/griddle -ti griddle-build <gradle args>`

where \<gradle args> will most commonly simply be `build`


  
