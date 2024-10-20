# MetaMart UI

> This guide will help you run MetaMart UI locally in dev mode.

## Pre-requisites

Before proceeding, ensure that you have installed the node and yarn with the versions given below.

```
"node": ">=18.19.0",
"yarn": "^1.22.0"
```

Install [Node](https://nodejs.org/en/download/) and [Yarn](https://classic.yarnpkg.com/lang/en/docs/install/).<br />

Install ANTLR using our recipes via

```shell
sudo make install_antlr_cli
```

Using the command below, spin up the server locally from the directory `metamart-dist/target/metamart-*-SNAPSHOT`

```shell
./bin/metamart-server-start.sh conf/metamart.yaml
```

> If you don't have distributions generated or don't see `target` directory inside the `metamart-dist` then follow [this](https://docs.meta-mart.org/developers/contribute/build-code-and-run-tests/metamart-server#create-a-distribution-packaging) guide to create a distribution.
>
> Since typescript is heavily used in the MetaMart project, we generate the typescript types and the interface from JSON schema. We use the `QuickType` tool to generate the typescript types and interfaces. You can view the complete instructions [here](https://docs.meta-mart.org/developers/contribute/build-code-and-run-tests/generate-typescript-types-from-json-schema).

Alternatively, you can connect to an already started MetaMart Server to develop UI by setting the `DEV_SERVER_TARGET` environment variable.
```shell
# For example, the metamart server service launched with docker compose:
# https://github.com/meta-mart/MetaMart/blob/main/docker/development/docker-compose.yml
export DEV_SERVER_TARGET=http://metamart-server:8585/

# Follow the steps to Run MetaMart UI...
make yarn_start_dev_ui
```

## Steps to Run MetaMart UI

Once the node and yarn are installed in the system, you can perform the following steps to run MetaMart UI.

**Step 1**: Run the given command to install the required dependencies.

**Note**: It’s a one-time task to install dependencies. If there are any changes in the `package.json` file, the following steps will have to be performed again.

```shell
# installing dependencies
> make yarn_install_cache
```

**Step 2**: Start the UI locally

```shell
# starting the UI locally
> make yarn_start_dev_ui
```

**Step 3**: Visit [localhost:3000](http://localhost:3000/) to access the MetaMart UI.

## How to Add Language Support

To add support for a new language in our internationalization setup using `react-i18next` and `i18next`, please follow the steps below:

### Create a Language JSON File

First, create a new JSON file for the language you want to add in the `metamart-ui/src/main/resources/ui/src/locale/languages` directory.

For example, if you want to add support for the `French` language, you can create a file called `fr-fr.json` in the languages directory:

```shell
# Navigate to the ui/src/locale/languages directory
cd metamart-ui/src/main/resources/ui/src/locale/languages

# Create the French language file
touch fr-fr.json

```

### Sync the Language File with the Primary Language

To ensure consistency with our primary language, which is `en-us`, it is necessary to synchronize any newly added language files. This can be done by copying the content from the `en-us.json` file and translating it accordingly.

To copy the contents of en-us.json and add it to your translation JSON file, follow these steps:

- Go to [en-us.json](https://github.com/meta-mart/MetaMart/blob/main/metamart-ui/src/main/resources/ui/src/locale/languages/en-us.json)
- Copy the content of file
- Open your translation JSON file.
- Paste the copied text into your translation JSON file.

You can refer to the image below for a visual guide:

![image](https://user-images.githubusercontent.com/59080942/227428589-5770b06e-f88d-4a8c-8c45-35ed12f0c4d2.png)
