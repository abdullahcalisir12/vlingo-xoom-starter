<script>
	import {
		Button,
		TextField,
		Dialog,
		CardActions
	} from 'svelte-materialify/src';
	import { aggregateSettings, currentAggregate, setLocalStorage } from "../../stores";
	import { classNameRule, identifierRule, requireRule, routeRule, isPropertyUnique, isAggregateUnique } from "../../validators";

	import StateFields from './StateFields.svelte';
	import Events from './Events.svelte';
	import Methods from './Methods.svelte';
	import Routes from './Routes.svelte';
	import ProducerExchange from './ProducerExchange.svelte';
	import ConsumerExchange from './ConsumerExchange.svelte';
	import ObjectValues from './ObjectValues.svelte';

	export let dialogActive;
	export let editMode;

	export let currentId;
	
	let aggregateIndex = undefined;
	let aggregateName = $currentAggregate ? $currentAggregate.aggregateName : "";
	let stateFields = [{ name: "id", type: "String" }];
	let events = [];
	let methods = [];
	let rootPath = "/";
	let producerExchangeName = "";
	let consumerExchangeName = "";
	let schemaGroup = "";
	let disableSchemaGroup = false;
	let routes = [];
	let outgoingEvents = [];
	let receivers = [];

	const add = () => {
		if(requireRule(aggregateName)) return;
		$aggregateSettings = [...$aggregateSettings, $currentAggregate];
		currentId = undefined;
		reset();
		dialogActive = false;
	}

	const update = () => {
		if(requireRule(aggregateName)) return;
		$aggregateSettings.splice(currentId, 1, $currentAggregate);
		$aggregateSettings = $aggregateSettings;
		currentId = undefined;
		reset();
		dialogActive = false;
	}

	const reset = () => {
		aggregateName = "";
		stateFields = [{ name: "id", type: "String" }];
		events = [];
		methods = [];
		rootPath = "/";
		routes = [];
		producerExchangeName = "";
		schemaGroup = retrieveSchemaGroup();
		disableSchemaGroup = !canWriteSchemaGroup();
		outgoingEvents = [];
		consumerExchangeName = "";
		receivers = [];
	}

	const retrieveSchemaGroup = () => {
		return $aggregateSettings.length > 0 ? $aggregateSettings[0].producerExchange.schemaGroup : "";
	}

	const canWriteSchemaGroup = () => {
		return currentId == 0 || (schemaGroup == undefined && schemaGroup.length == 0);
	}

	$: changedCurrent(currentId);
	function changedCurrent(index) {
		aggregateIndex = index;
		if(index !== undefined && $aggregateSettings[index]) {
			const aggregateWithId =  $aggregateSettings[index];
			aggregateName = aggregateWithId.aggregateName;
			stateFields = aggregateWithId.stateFields;
			events = aggregateWithId.events;
			methods = aggregateWithId.methods;
			rootPath = aggregateWithId.api.rootPath;
			routes = aggregateWithId.api.routes;
			producerExchangeName = aggregateWithId.producerExchange.exchangeName;
			schemaGroup = aggregateWithId.producerExchange.schemaGroup;
			outgoingEvents = aggregateWithId.producerExchange.outgoingEvents;
			consumerExchangeName = aggregateWithId.consumerExchange.exchangeName;
			receivers = aggregateWithId.consumerExchange.receivers;
		} else {
			reset();
		}
	}

	const validField = (f) => !identifierRule(f.name) && f.type && !isPropertyUnique(f.name, stateFields, 'name');
	const validEvent = (e) => !classNameRule(e.name) && e.fields.length > 0 && !isPropertyUnique(e.name, events, 'name');
	const validMethod = (m) => !identifierRule(m.name) && m.parameters.length > 0 && m.event && !isPropertyUnique(m.name, methods, 'name');
	const validRoute = (r) => r.path && r.aggregateMethod;

	$: valid = !classNameRule(aggregateName) && stateFields.every(validField) && events.every(validEvent) && methods.every(validMethod) && !routeRule(rootPath) && routes.every(validRoute) && !isAggregateUnique(aggregateIndex, aggregateName, [...$aggregateSettings, { aggregateName }]);
	$: if(valid) {
		$currentAggregate = {aggregateIndex, aggregateName, stateFields, events, methods, api: { rootPath, routes }, producerExchange: { "exchangeName" : producerExchangeName, schemaGroup, outgoingEvents }, consumerExchange: {  "exchangeName" : consumerExchangeName, receivers } };
		//TODO: rework this - we need to keep the modal open, too.
		setLocalStorage("currentAggregate", $currentAggregate);
	}
</script>

<Dialog bind:active={dialogActive} persistent width={1000} class="pa-4 pa-lg-8 rounded">
	<h4 class="mb-5" style="text-align: center;">
		{#if editMode}
			Update Aggregate
		{:else}
			New Aggregate
		{/if}
	</h4>
	<TextField class="mb-4" bind:value={aggregateName} rules={[requireRule, classNameRule, (name) => isAggregateUnique(aggregateIndex, name, [...$aggregateSettings, { aggregateName }])]} validateOnBlur={!aggregateName}>Aggregate Name</TextField>
	<ObjectValues />
	<StateFields bind:stateFields />
	<Events bind:events  bind:stateFields />
	<Methods bind:methods bind:stateFields bind:events />
	<Routes bind:routes bind:methods bind:rootPath />
	<ProducerExchange bind:events bind:producerExchangeName bind:outgoingEvents bind:schemaGroup bind:disableSchemaGroup  />
	<ConsumerExchange bind:consumerExchangeName bind:receivers bind:methods />
	<CardActions>
		{#if editMode}
			<Button class="mr-3" on:click={update} disabled={!valid}>Update</Button>
		{:else}
			<Button class="mr-3" on:click={add} disabled={!valid}>Add</Button>
		{/if}
		<Button on:click={() => dialogActive = !dialogActive}>Cancel</Button>
		<span style="width: 100%;"></span>
		<Button on:click={reset}>Reset</Button>
	</CardActions>
</Dialog>