#!/bin/bash

au build --env prod
cp index.html ../public
cp scripts/*.js ../public/scripts
